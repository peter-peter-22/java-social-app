package com.example.uploads_api.uploads;

public enum UploadStatus {
    /** Accessible. Has a unique upload key. */
    READY,
    /** Deletion is in progress, some objects might be missing, not accessible. The upload key is no longer considered taken. */
    DELETING,
    /** All objects are deleted. */
    DELETED
}
