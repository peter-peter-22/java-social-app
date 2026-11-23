# The rules of upload lifecycle

## Goal
The goal of the defined upload lifecycle mechanisms is to ensure that there are no orphan files, accidental errors, stuck uploads, and other problems.

## Implementation
- Overwrite is disabled for all user uploads.
- All user uploads have a limited lifespan initially.
- They are registered to the database before their lifespan becomes permanent.
- The uploads have a limited max duration.
- The final upload registration is atomic and cannot overwrite.
- The upload signatures have a limited retry count.
- All files and transformations get a unique asset.
- Every worker execution allocates fresh result asset ids, so a retried transformation never reuses a key.
- The registered assets are immutable.
- The failed uploads are periodically cleaned up.
- The existence of the file is checked after their expiration is removed.
- The contents of the asset directories are recorded, other objects cannot be reached from the access methods of this asset.

## Practical problems

The scenarios describe the possible problems of the upload workflow and their solution. 

### Fail before finalization
The uploaded files expire because of their initial lifecycle policy. (expiration object tag)
The registration of the stored asset remains and the periodic cleanup will delete it.

### The worker generates an existing asset id
The worker does not interact with the database during the asset id generation, so overlapping ids are possible.
In this case, the forbidden overwrite policy prevents destructive changes.
The object expiration prevents permanent orphan objects on failure. 
The finalization checks if the asset ids are actually valid before removing the expiration.

In the worst case, the work fails at the finalization step, meaning that the transformation time and cost is doubled.
The chance of the overlapping UUIDv4s is excepted to be very small, this probably won't require further optimization.

### Fail during upload
The upload can be retried from the nginx proxy that buffers the uploads, or the browser.
The registration of the upload attempt and its assets might be present in the database.
The asset registries are cleaned up periodically.

### What happens to the failed upload attempts
The registration of the upload attempts are kept, they don't interfere with further actions or take too much space.
Adding another expiration and cleanup for the attempts would add unnecessary complexity.

### The upload is retried
All uploaded assets get unique identity, the new attempt won't overlap with the previous ones.

### Spamming retries
The upload signature has limited usage attempt, this prevents abuse.

### Fail after finalization
The finalized permanent files remain registered in the database until they are deleted by the periodic cleanup.

### Fail during finalization.
The asset should be stuck in "FINALIZING" state and the periodic cleanup removes it along with its files.
This works the same way as failing after finalization, the point is the permanent files might be present.

### An upload is stuck or lasts too long
The maximum upload duration is limited to 1 day by the object storage.
(This is the shortest possible value.)

### Fail while writing the final upload registry
The final write checks for existence, and it's done in a transaction.
Any duplication or invalid value causes constraint error and transaction rollback.

### The blocking transformation fails
The HTTP client retries transport failures and worker server errors up to a bounded count.
Every worker execution allocates fresh result asset ids, so a retry writes to new object keys and
does not collide with the files of a failed execution.
When the retry count is exceeded, the attempt is marked FAILED and the request fails.
Unrecoverable errors, such as a 4xx answer from the worker, are not retried.

### The async transformation fails
The message queue retries it. Every delivery is a new worker execution with fresh asset ids, so a
redelivery after a partial write is safe.
Exceeding the retry count or reaching an unrecoverable error marks the attempt as FAILED.
Completion is idempotent on the attempt id.

### A file expires during finalization
The existence check after finalization fails.

### A file expires before finalization
Altering the lifespan of a missing file causes error.
// TODO verify

### An upload is stuck for a long time, it's database records are deleted, and another upload takes it's key by accident
The uploads cannot be overwritten, no effect.

### An upload is stuck for a long time, it's database records are deleted, and it tries to write to a valid but empty object key in a directory type asset
The contents of a directory type asset are registered.
If the file will be written with the valid asset prefix, the access for this file will be blocked because it's not a registered content of this asset.
Since it's asset record no longer exists (two assets cannot overlap otherwise) the object cannot be permanent and it will expire.

### An expired object is replaced by a de-registered, slow upload
This would require to:
- 2 assets those got the same UUID
- one asset failed and it's registration was removed
- the failed asset was in uploading state when it was removed, so it's ongoing upload cannot be deleted
- the other asset is waiting since the expiration time that is 1 day (the upload should be considered expired far before this, so it should survive multiple periodical cleanups)
- the upload of the failed asset is still going on after 1 day (we imply the cleanup of the too long multipart uploads can be delayed)
- either the client or the upload worker is waiting until the original object expires

If this happened, an object would be replaced with another.

Possible fixes:
- Avoid the finalization of assets those are older than the expiration time.
- Never delete the asset ids of failed uploads.

### A transformation task is retried
The worker allocates a fresh asset id and prefix for every output on every execution.
The files of a failed execution stay under prefixes that are never registered; they are temporary
and expire.
The retry writes to new object keys and cannot overwrite anything.
Only the successful execution's assets are registered and made permanent.
