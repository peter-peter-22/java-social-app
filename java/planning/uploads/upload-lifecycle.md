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
- The registered assets are immutable.
- The failed uploads are periodically cleaned up.
- The existence of the file is checked after their expiration is removed.
- The contents of the asset directories are recorded, other objects cannot be reached from the access methods of this asset.

## Practical problems

The scenarios describe the possible problems of the upload workflow and their solution. 

### Fail before finalization
The uploaded files expire because of their initial lifecycle policy. (expiration object tag)
The registration of the stored asset remains and the periodic cleanup will delete it.

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
The HTTP client retries it.
TODO When exceeding the retry count the request fails with unrecoverable error.
TODO Unrecoverable errors in the media worker are not retried by the HTTP client that communicates with it.

### The async transformation fails
The message queue retries it.
TODO Exceeding the retry count or reaching an unrecoverable error must mark the upload as failed.

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
The chance for this is too small, effectively 0.
This would require to:
- 2 assets those got the same UUID
- one asset failed and it's registration was removed
- the failed asset was in uploading state when it was removed, so it's ongoing upload cannot be deleted
- the other asset is waiting since the expiration time that is 1 day (the upload should be considered expired far before this, so it should survive multiple periodical cleanups)
- the upload of the failed asset is still going on after 1 day (we imply the cleanup of the too long multipart uploads can be delayed)
- either the client or the upload worker is waiting until the original object expires

If this happened, an object would be replaced with another.
Checking the object hashes after finalization could prevent this, but we consider this situation unreachable.