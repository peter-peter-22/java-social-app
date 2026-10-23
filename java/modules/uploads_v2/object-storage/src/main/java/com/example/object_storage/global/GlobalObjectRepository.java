package com.example.object_storage.global;

import com.example.object_storage.regional.RegionalObjectRepositories;
import com.example.object_storage.regional.args.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Executors;

@Repository
@NullMarked
@RequiredArgsConstructor
public class GlobalObjectRepository {
    private final RegionalObjectRepositories regionalObjectRepositories;

    public void download(String region, DownloadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).downloadObject(args);
    }

    public void upload(String region, UploadLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).uploadObject(args);
    }

    public void uploadAll(String region, Collection<UploadLocalObjectArgs> args) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (UploadLocalObjectArgs arg : args) {
                executor.submit(() -> upload(region, arg));
            }
        }
    }

    public void delete(String region, DeleteLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).deleteObject(args);
    }

    public InputStream getObject(String region, GetLocalObjectArgs args) {
        return regionalObjectRepositories.getByRegion(region).getObject(args);
    }

    public void putObject(String region, PutLocalObjectArgs args) {
        regionalObjectRepositories.getByRegion(region).putObject(args);
    }

    public String getHomeRegion() {
        return regionalObjectRepositories.getHomeRegion();
    }

    /**
     * Helper for variants: compute deterministic object key/path.
     * Use with uploadKey + transformationName + version to ensure idempotency.
     * For directories, the key is a prefix; use delete with prefix support in regional impl if added.
     */
    public static String variantObjectKey(String uploadKey, String transformationName, String versionHash, String filename) {
        return "variants/" + uploadKey + "/" + transformationName + "/v" + versionHash + "/" + filename;
    }

    /**
     * For directory variants (e.g. HLS), delete all objects under the prefix.
     * Must be called under DB row lock on the VariantEntity to avoid races/orphans.
     * (Current regional delete is single-key; extend with listObjects + removeObjects for prefix if needed.)
     */
    public void deleteVariantObjects(String region, String uploadKey, String transformationName, String versionHash) {
        // Placeholder: implement prefix delete via regionalObjectRepositories when full support added.
        // For now, delete known entryPoint or main object.
        String prefix = "variants/" + uploadKey + "/" + transformationName + "/v" + versionHash + "/";
        // regional... .removeObjects with prefix filter (MinIO supports RemoveObjects with filter).
        delete(region, DeleteLocalObjectArgs.builder().bucket("media").key(prefix).build()); // update as needed
    }
}
