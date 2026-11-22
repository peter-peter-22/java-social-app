# Media JSON

The JSON shape of the contract types. Jackson mixins and the shared mapper configuration
(`MediaJson`) that the web, the registry's jsonb columns, and the future message queue all
serialize through. The shape tests in this module are the lock.

No Spring, JPA, or Minio: this module is pure Jackson and the only place that knows Jackson.
The domain types in `media-contract` carry no serialization annotations.