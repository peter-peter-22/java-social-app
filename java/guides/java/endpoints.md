# Web MVC endpoints

## File upload
```java
    @PostMapping(path = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void uploadImage(
            @Valid @RequestPart("body") ImageUploadRequest body,
            @RequestPart("file") MultipartFile file
    ) {
        //...
    }
```

The RequestPart JSON means that the JSON string is uploaded as a multipart file.
This is the standard way of uploading JSON along with files with multipart form data.
