package com.example.uploads_service_4.upload_service_2;

import com.example.uploads_persistence.upload_repository.Upload;

import java.io.InputStream;


public abstract class UploadService<AbstractUpload extends Upload> {
    AbstractUpload upload(UploadRequest uploadRequest, InputStream inputStream) {
        // create upload row with pending status
        // upload to the global object storage repository
        // if blocking: complete the transformations, write results, probe, status, files from the worker, return the response
        // if async: queue the async transformations, write results, probe, status, files from the worker, return the response
        // the response contains only the generic file metadata, the format specific info is added by the worked into separate tables
        return null;
    }

    public InputStream read(String uploadKey) {
        // get object key and region from the table
        // exit if marked for deletion
        // return input stream from global object repository (handles fallback internally)
        // optionally handle locking for deduplicated fallbacks and write
        return null;
    }

    public void delete(String uploadKey) {
        // update upload status in the table, mark for deletion
        // delete in local object storage
    }
}
