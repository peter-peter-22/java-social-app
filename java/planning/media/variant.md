# Upload variants

## Expected behavior

### Getting
One variant can be queried based on the upload key and transformation name those were used for its creation.

### Identity and uniqueness
Only one combination of the same upload key and transformation name can exist at once.
When a new variant for this combination is created, the previous one is overwritten/deleted.
The unique identity of a variant is upload key + transformation name.

### Transformation versions
When a named transformation changes parameters, changing the active transformation version of this name,
the created variants are not affected, and they continue to be visible.
The variants save which transformation version was used for their creation to track what happened
and how to apply migrations, but this is handled outside the media cloud.

### Eager transformations
The variants created by the eager transformations are processed together, and they are considered ready when all of them are complete
regardless of the synchronous or asynchronous processing.

### Eager transformation retries
When an eager transformation is retried due to some error, the process must ensure that the previous transformation is not running and won't accidentally overwrite
the new one.
Some sort of unique identity and progress tracking will be necessary for the eager transformation of the uploads.
A separate table that tracks the pending transformation and uploads will might be necessary.
A unique key to each attempt will might be necessary to check to make an asynchronous process to know
that it was supposed to be canceled and cannot write the result.

### Eager transformation unrecoverable error
When an error does not cause a retry, the whole upload is considered failed
because it can be ready only after all transformation are done.
The 

### Manual variant creation
Transformations can be started manually from the admin API.
When a transformation is requested from the admin API for an existing transformation name + upload key pair, the
variant is overwritten in the registry only when the whole thing is complete.
The access for this variant won't be blocked during the process.

## Practical use cases