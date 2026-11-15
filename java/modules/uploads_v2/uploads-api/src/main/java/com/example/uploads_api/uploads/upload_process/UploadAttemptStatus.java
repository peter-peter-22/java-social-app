package com.example.uploads_api.uploads.upload_process;

public enum UploadAttemptStatus {
    /** An attempt was created. It's either being processed or failed. */
    PENDING,
    /** An attempt resulted in the creation of an upload. */
    SUCCESS
}
