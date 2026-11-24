# Object storage module

## Overview

This module contains the codes for interacting with the Minio object storage.
The bucket creations and the region clients are here.

## The Minio adapter of the clean media service

This module is the Minio adapter of the clean media service in `modules/media`:
`MinioObjectStore` implements the `ObjectStore` port of `media-contract`.
See `planning/uploads/clean-media-service.md`.

The Minio argument types (`regional.args`), the tags, and the reconciliation stay inside this
module. Application services use the `ObjectStore` port only; the `ObjectLifetime` of the contract
is the only lifecycle knob they get.