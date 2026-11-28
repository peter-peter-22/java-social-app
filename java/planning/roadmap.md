# User roadmap

This roadmap is a high level overview of the planned features or topics.
The execution of the steps is not ordered, the order is depending on which one is the easiest to implement at the given state.

# Baseline application

- [x] Revise the testing architecture and the project structure.

- [ ] Find a reliable cleanup mechanism.

- [ ] Add the video and raw uploads.

- [x] Build a minimal functioning media cloud.

- [ ] Test multi region object storage behavior in reproducible integration test.

- [ ] Build a minimal functioning post creation web API.

- [x] Replace the @NotNull annotations with @Nullmarked. (Beware of the Jakarta validation not null.)

- [ ] Build a minimal client, enough to test the full post and file upload.

- [x] Implement upload read authentication.

- [ ] Implement live streams.

- [x] Re-read and merge the completed upload-authorization and testing plans into the documentation.

- [ ] Validate the whole upload code now that the upload authentication is complete.

- [ ] Plan the complete project shape and conventions (API gateway setup, openAPI, user authentication, conventions, observability) after the media service is and a baseline post service is ready.

- [x] Investigate the HMAC key rotation and the admin client key.

- [ ] Investigate how a realistic modern microservice application works. (How the internal communication works. Edge or cluster deployment? How security and authentication is done?)

- [ ] Add the version number to the media read URL paths, not to the URL query. The cache of the media read URLs should be immutable, so the version must be a part of it. 

- [ ] The image transformation tests are over-complicated. Generating images and checking the colors of the transformations is hard to maintain and extend. A pre-defined image and dimension checks would be more simple.

- [ ] Revise the water-falling database queries and transformations in the media service. 

- [ ] Edit the null safety rule. Nullmarked → package-info

- [ ] In `CompleteImageUpload.registerResultAsset` the delivery protocol is hard-coded. For an image, this works, but a video can use `VIDEO` and `HLS_CMAF`. The delivery protocol should be defined by the encoding of the transformation when registering variants. This must be fixed before the video transformations can work.

- The CDN caching, authentication and other details are deferred until a test build can be deployed on a real server provider and the CDN service is chosen. The current decision about the CDN will might be overwritten. The current nginx HMAC authentication is considered as a proof-of-concept prototype.

# Verify the behavior of the used services

Some behavior for the used services is assumed and need to be tested.
- [x] Check if the Minio "If-None-Match:*" header blocks the completion of a multipart upload when it was made before another upload was written. 
- [ ] Check how the Minio replication works between regions. 
- [ ] Check if the Minio object life-cycle policies are actually applied to multipart uploads and existing temporal objects.
- [ ] Check how the CockroachDB replication works between regions.

These tests should be done with container tests those are not parts of the main application tests.
The tests are excepted to be slow, running them regularly along with the other tests must be avoided.
After confirming what happens, a test should be created that checks if the service still works as intended after a possible replacement or update.

# Seek improvements

Validate the seemingly pointless usage of transactions and flushing on some repositories.

Check the advantages of package scoped constructors. (probably useless)

Seek for improvements in the microservice architecture.
- gRPC? 
- [x] Arch unit 
- JDK 28 value classes?

# Explore real deployment and architectures

How the complete deployment will work?

Discover kubernetes, AWS, Azure, ect.

Complete the architecture example tutorials.

# Expand

Implement the rest of the application.
Excepted:
- Post feed with ranking and fanout.
- Direct messages.
- Gmail authentication.