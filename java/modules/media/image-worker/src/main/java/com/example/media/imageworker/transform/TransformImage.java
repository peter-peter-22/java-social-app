package com.example.media.imageworker.transform;

import com.example.media.contract.asset.AssetId;
import com.example.media.contract.object.ObjectLifetime;
import com.example.media.contract.port.ObjectStore;
import com.example.media.contract.worker.CompletedImageOutput;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import com.example.media.contract.worker.PlannedImageOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The transform use case of the worker: download the source, transform it, upload the results
 * under newly allocated asset prefixes, and return the result.
 *
 * <p>The worker owns no lifecycle: every result object is written as
 * {@link ObjectLifetime#TEMPORARY} and the result reports what was produced. The API removes the
 * expiration tags, checks the objects, registers the rows, and finalizes the attempt in one
 * transaction, so a failed run leaves only objects that expire on their own.</p>
 *
 * <p>Every execution allocates a fresh asset id and prefix for each output. A retried execution
 * therefore never tries to overwrite objects from an earlier, partially completed execution. The
 * outputs are reported in the command's order, which is the wire contract with the API.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@NullMarked
public class TransformImage {
    private final ObjectStore objectStore;
    private final ImageProcessing imageProcessing;

    /** Runs one transform command and reports the produced outputs and the source dimensions. */
    public ImageTransformResult transform(ImageTransformCommand command) {
        var workDir = createWorkDirectory();
        try {
            var source = workDir.resolve("source");
            objectStore.download(command.source(), source);
            var sourceMetadata = imageProcessing.readMetadata(source);

            var outputs = new ArrayList<CompletedImageOutput>();
            for (var planned : command.outputs())
                outputs.add(processOutput(workDir, planned, source));
            return new ImageTransformResult(command.attemptId(), outputs, sourceMetadata);
        } finally {
            deleteWorkDirectory(workDir);
        }
    }

    /**
     * Transforms the source for one planned output and uploads the produced files under a freshly
     * allocated asset prefix. The fresh identity is what makes retries safe.
     */
    private CompletedImageOutput processOutput(Path workDir, PlannedImageOutput planned, Path source) {
        var assetId = AssetId.generate();
        var prefix = planned.placement().prefixOf(assetId);
        var outputDir = workDir.resolve(assetId.value().toString());
        createOutputDirectory(outputDir);
        var processed = imageProcessing.process(source, outputDir, planned.spec());

        var files = producedFiles(outputDir);
        var objects = files.stream().map(file -> outputDir.relativize(file).toString()).toList();
        var contentType = planned.spec().encoding().contentType();
        for (var file : files)
            objectStore.put(
                    prefix.resolve(outputDir.relativize(file).toString()),
                    file,
                    contentType,
                    ObjectLifetime.TEMPORARY);

        return new CompletedImageOutput(
                assetId,
                prefix,
                outputDir.relativize(processed.entryPoint()).toString(),
                objects,
                totalBytes(files),
                contentType,
                processed.metadata());
    }

    /** The work files live in the default temp directory and are deleted when the run ends. */
    private static Path createWorkDirectory() {
        try {
            return Files.createTempDirectory("image-worker");
        } catch (IOException e) {
            throw new UncheckedIOException("could not create the work directory", e);
        }
    }

    private static void createOutputDirectory(Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new UncheckedIOException("could not create the output directory: " + outputDir, e);
        }
    }

    /** The produced files of one output, in a stable order. */
    private static List<Path> producedFiles(Path outputDir) {
        try (var paths = Files.walk(outputDir)) {
            return paths.filter(Files::isRegularFile).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("could not list the produced files of: " + outputDir, e);
        }
    }

    private static long totalBytes(List<Path> files) {
        try {
            var total = 0L;
            for (var file : files)
                total += Files.size(file);
            return total;
        } catch (IOException e) {
            throw new UncheckedIOException("could not measure the produced files", e);
        }
    }

    private static void deleteWorkDirectory(Path workDir) {
        try (var paths = Files.walk(workDir)) {
            for (var path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete the work directory: {}", workDir, e);
        }
    }
}