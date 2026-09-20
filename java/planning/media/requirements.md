# Media service

## Introduction

A group of microservices for handling media uploads and transformations.
A Cloudinary clone.

Compared to Cloudinary, there are only strict transformations here.
This means that there is a global registry of what named transformation can be requested
on-demans. Other on-demand transformations are not allowed. Any other
transformation can be used only from signed upload/get URLs or the admin API.

## Architecture

- Main gateway: All requests reach this API first. The request is routed to the image/video/raw workers.
- Media workers: Image/video/raw files have separate workers because images and videos require different processing
  tools and hardware (Lipvips, Ffmpeg)
- Database: The id, home region, metadata and other parameters of the uploads are stored here.
- Message queue: The async transformations are queued here.
- Object storages: One Minio object storage cluster per region for the files.

## Features

### File access

The home region of the uploaded files is recorded.

Access sequence:

- Get the home region from the database based on the upload key.
- Check the closest object storage.
- If the file is there, return, if not, query the home region and store locally.

A file can be private.
A private file can be accessed only from signed GET requests or the admin API.

The files in the object storage are replicated asynchronously in addition to the fallbacks.

### Transformations

Transformations can be applied to images and videos.
The transformations can change the resolution, apply filters, change the format.
The transformations have multiple formats, and they are applied at different events.
One file can have multiple transformations.
Some transformations can generate multiple files, e.g., HLS video format.

Every transformation has a name or string that is used in the URL when they are accessed.

#### Transformation formats

##### Transformation object:

This is how the transformation parameters are stored internally.
Contains only the transformation instructions.

##### Transformation string:

Stringified transformation object.

##### Named transformation:

A transformation object stored in the database with a given name.
In addition to a transformation, it has the "explicit" parameter.
This decides if the changes are applied retroactively when the parameters
are updated on this named transformation.

##### Upload preset:

A group of named transformations.
The upload preset can also define the visibility.
The parameters of the upload preset can be overwritten by signed upload parameters.

#### When a transformation is applied?

- Eager: After a file is uploaded. Can be sync or async.
- Incoming: Applied before an upload is stored (so overwrites the original)
- On-demand: Generated when getting the file and then stored.
- Admin: The admin API can directly request a transformation.

### Admin API

A java API client with api key.

Functions:

- Creates signed upload keys.
- Direct upload / get / delete.
- Creates named transformations and upload presets.

### Client API

TypeScript client for the browser.
It can generate upload/get URLs from the given parameters.
It can also construct transformation string and add them to the URLs.
It contains pre-configured video/image displayers for convenience.

### Signed upload

The user can upload any file from the client to the related upload endpoints.
There are separate endpoints for images, videos and raw files.
The signature can define the file size, upload location, visibility, and format to make sure
the user is uploading what the signature was requested for.

The sequence:

- The user requests a signed upload token from the main app.
- The main app check the request and generates a signature.
- The signature, the upload parameters, and the file are uploaded to the media API.
- The request is routed to the related worker.
- Verify the signature.
- Apply incoming transformations if any.
- Store the file in the object storage.
- Apply the eager transformations if not async or queue if async.
- Call the webhook if requested in the upload parameters.

### Signed download

In addition to plain access, a signed upload can apply on-demand transformations and access private files.

The sequence:

- The user requests a signed download url from the main app.
- The app creates a signature and builds an access URL.
- The access URL restricts the upload key and applied transformations.
- The client uses this URL to get a file or a variant.

### Deletion

The deletion marks a file deleted in the database.
The deleted upload and all of its variants will be unaccessible regardless of their actual presence
in the object storage.
The file is deleted in the object storage, this will eventually remove it from
all regional clusters.