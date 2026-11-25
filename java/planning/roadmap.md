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

- [ ] Replace the @NotNull annotations with @Nullmarked. (Beware of the Jakarta validation not null.)

- [ ] Build a minimal client, enough to test the full post and file upload.

- [ ] Implement upload read authentication.

- [ ] Implement live streams.

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

# Seek improvements

Validate the seemingly pointless usage of transactions adn flushing on some repositories.

Check the advantages of package scoped constructors.

Seek for improvements. (gRPC? Arch unit? value classes?)

# Explore real deployment and architectures

How the complete deployment will work?

Discover kubernetes, AWS, Azure, ect.

Complete the architecture example tutorials.

# Expand

Implement the rest of the application