# Baseline application
Consider to use database rows instead of encrypted signatures for the upload permissions.

Build a minimal functioning media cloud.

Check if the multi region object storage is used properly.

Build a minimal functioning post creation web API.

Replace the @NotNull annotations with @Nullmarked. (Beware of the Jakarta validation not null.)

Build a minimal client, enough to test the full post and file upload.

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