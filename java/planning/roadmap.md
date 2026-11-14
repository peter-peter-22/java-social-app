# Baseline application
Consider to use database rows instead of encrypted signatures for the upload permissions.

Make the image uploads alone to work.

Add the video and raw uploads.

Build a minimal functioning media cloud.

Do a complete refractor to make the code readable enough.

Check if the multi region object storage is used properly.

Build a minimal functioning post creation web API.

Replace the @NotNull annotations with @Nullmarked. (Beware of the Jakarta validation not null.)

Build a minimal client, enough to test the full post and file upload.

Implement upload read authentication.

Implement live streams.

# Verify the behavior of the used services

Some behavior for the used services is assumed and need to be tested.
- Check if the Minio "If-None-Match:*" header blocks the completion of a multipart upload when it was made before another upload was written. 
- Check how the Minio replication works between regions. 
- Check if the Minio object life-cycle policies are actually applied to multipart uploads and existing temporal objects.
- Check how the CockroachDB replication works between regions.

These tests should be done with container tests those are not parts of the main application tests.
The tests are excepted to be slow, running them regularly along with the other tests must be avoided.
After confirming what happens, a test should be created that checks if the service still works as intended after a possible replacement or update.

# Prepare clean up
Rebuild the documentation.

# Clean up
Refractor code and Gradle files, check architecture, check folder structure.

Validate the seemingly pointless usage of transactions adn flushing on some repositories.

Refine lombok usage. (Add @Value annotations where beneficial, explore other shortcuts.)

Check the advantages of package scoped constructors.

Validate the used tools and methods.

Implement hexagonal and microservice architectures properly.

Seek for improvements. (gRPC? Arch unit? value classes?)

# Explore real deployment and architectures

How the complete deployment will work?

Discover kubernetes, AWS, Azure, ect.

Complete the architecture example tutorials.

# Expand

Implement the rest of the application