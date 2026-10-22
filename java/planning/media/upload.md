# Uploads plan

## Excepted behavior

### Uploading
An upload can be created by a user with an upload request that has a valid signature.
The file, the parameters and the signature are uploaded together.
Eager and incoming transformations happen after upload.

### Reading
When reading from the browser client, the upload service returns the searched file from the object storage.
Some uploads require authentication.
In this case, the use needs a signature from the server to access a file.

### Overwriting
Uploads cannot be overwritten.
They need to be completely deleted before they can be written again.
This changes their version number, invalidating their cache in the browser.

### Caching
The uploads and their variants are cached forever, but their access URL contains their version number the is derived from the date of modification/creation.
The version of the uploads is tracked based on the upload registry of the main app.
The client of the upload application must be able to generate URLs instead of trusting that the main app build use the right URLs similarly to Cloudinary.

### Deletion
Deletion must happen by marking an upload as deleted in the database and cleaning up the object storage later.

### Upload keys
The upload keys are the unique identity of an upload.
Currently, it is string to make sure it's flexible and to follow the example of Cloudinary.

If I consider that the media service is specialized for this application only instead of general usage, then using
UUID + some sort of tags could be slightly more convenient.
The main application must provide a unique ID for the upload when it creates an upload signature, so it can be
both string prefix + UUID or only UUID.
The string prefix of the upload keys only serve organization purposes, they have no functional impact, replacing them with tags
is possible.
Using UUIDs would provide a

## Practical use cases

### User upload
The user asks for an upload signature from the main application. 
The main application determines if the user is eligible to publish a new upload.
The main application registers the upload key of the pending upload.
The main application responds with the upload signature and other upload parameters, e.g., transformations and visibility.
The user uploads the file and the parameters to the upload service.
The upload service processes the upload requests, and according to the intended usage it calls a webhook on the main application that 
marks the pending upload as ready, and forwards some additional metadata (e.g., format, resolution).
If the upload had async eager transformations, the request ends before the upload is considered ready, the client
decided what to do here. (intended usage: block the publishing of a post until the uploads are ready, or queue the publishing)

An alternative to the webhook would be to check if the uploads are ok only when the user attempts to publish the post by requesting
information about the listed uploads from the media service.

### User read
The user can read the original upload from the media service based on the upload key, and a variant based on the upload key and the transformation name.
Before reading, the browser client gets the upload key and display protocol of the media files typically from the main application.
(For example, the posts and users contain their media records.)
The upload service read endpoints return only media files.
Authentication can be used here.
Using on-demand transformations here is possible, but this feature probably won't b used, removing it entirely might be a better choice than wasting time with it as long
as the upload service is capable of implementing this later.

### Read authentication
A read might require authentication. 
In this case, a cryptographic signature from the server.
The exact was of implementing the verification of the signature on the CDN is not planned yet.
The browser client or the CDN could request a new read signature when the current one is expired or missing.
This feature is unstable, but authentication is necessary.
The goal of the read authentication is to make it possible to hide the uploads of direct messages and group chats from
the others.