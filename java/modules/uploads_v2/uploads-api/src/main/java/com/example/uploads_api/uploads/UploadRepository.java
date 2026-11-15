package com.example.uploads_api.uploads;


import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface UploadRepository {
    UUID insert(InsertUpload upload) throws UploadKeyTakenException;

    Upload findByKey(String uploadKey);
    Upload findImageByKey(String uploadKey);
    Upload findVideoByKey(String uploadKey);

    // TODO add deletion:
    //  the deletion should delete the upload, its variants and set the status of all stored assets to DELETING
    //  it should use the delete function of the variant repository
    //  must be transacitonal
}
