package com.example.media.imageworker.transform;

import com.example.media.contract.asset.ImageMetadata;
import com.example.media.contract.transformation.AspectRatio;
import com.example.media.contract.transformation.ImageEncoding;
import com.example.media.contract.transformation.ImageTransformationSpec;
import com.example.media.contract.transformation.LimitResolution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The transformation output lock: the real libvips processing against generated fixture images.
 * The assertions are structural — dimensions of crop and pad, resolution limits, encoding
 * round-trips, sampled pixel regions — not byte-exact goldens, because the output bytes drift with
 * the libvips version. Every output is copied to {@code build/reports/transformations/} for visual
 * inspection, which revives the removed docker test's purpose with automatic assertions.
 *
 * <p>libvips is loaded through the foreign function API, so this suite must run where libvips is
 * installed: {@code deployment/test-transformations.bash} runs it in the JDK + libvips development
 * image. It is not part of the normal unit-test run.</p>
 */
class ImageProcessorTest {

    private final ImageProcessor processor = new ImageProcessor();

    @TempDir
    Path tempDir;

    private final Path reportDir = Path.of("build", "reports", "transformations");

    private static final ImageTransformationSpec JPEG_95 =
            new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(95));

    @Test
    void readMetadataReportsTheDimensionsOfTheSource() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);

        var metadata = processor.readMetadata(source);

        assertThat(metadata).isEqualTo(new ImageMetadata(40, 20));
    }

    @Test
    void jpegOutputIsNamedDefaultJpegAndKeepsTheDimensions() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);

        var processed = transform("jpeg-encoding", source, JPEG_95);

        assertThat(processed.entryPoint().getFileName().toString()).isEqualTo("default.jpg");
        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(40, 20));
        assertJpegMagic(processed.entryPoint());
        var decoded = ImageIO.read(processed.entryPoint().toFile());
        assertThat(decoded.getWidth()).isEqualTo(40);
        assertThat(decoded.getHeight()).isEqualTo(20);
    }

    @Test
    void webpOutputIsNamedDefaultWebpAndKeepsTheDimensions() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(null, null, null, new ImageEncoding.Webp(90));

        var processed = transform("webp-encoding", source, spec);

        assertThat(processed.entryPoint().getFileName().toString()).isEqualTo("default.webp");
        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(40, 20));
        assertWebpMagic(processed.entryPoint());
        // the encoded webp round-trips through the real vips reader with the same dimensions
        assertThat(processor.readMetadata(processed.entryPoint())).isEqualTo(new ImageMetadata(40, 20));
    }

    /** FILL crops around the center: the 40x20 source keeps a centered 20x20 window. */
    @Test
    void fillCropsTheSourceCenteredToTheRatio() throws IOException {
        var source = splitJpeg("source", 40, 20, Color.RED, Color.BLUE);
        var spec = new ImageTransformationSpec(
                null, null, new AspectRatio(1, 1, AspectRatio.Mode.FILL), new ImageEncoding.Jpeg(95));

        var processed = transform("fill-crop", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(20, 20));
        var decoded = ImageIO.read(processed.entryPoint().toFile());
        // the crop window is source x=[10,30): the left of the output is red, the right is blue
        assertPixel(decoded.getRGB(3, 10), "red");
        assertPixel(decoded.getRGB(16, 10), "blue");
    }

    /** CONTAIN pads with black: the 40x20 source becomes 40x40 with black bars top and bottom. */
    @Test
    void containPadsTheSourceWithBlackToTheRatio() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(
                null, null, new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN), new ImageEncoding.Jpeg(95));

        var processed = transform("contain-pad", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(40, 40));
        var decoded = ImageIO.read(processed.entryPoint().toFile());
        assertPixel(decoded.getRGB(1, 1), "black");
        assertPixel(decoded.getRGB(20, 20), "red");
    }

    @Test
    void limitWidthScalesDownKeepingTheAspectRatio() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(
                new LimitResolution(20, LimitResolution.Mode.KEEP_ASPECT_RATIO), null, null, JPEG_95.encoding());

        var processed = transform("limit-width", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(20, 10));
    }

    @Test
    void limitHeightScalesDownKeepingTheAspectRatio() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(
                null, new LimitResolution(10, LimitResolution.Mode.KEEP_ASPECT_RATIO), null, JPEG_95.encoding());

        var processed = transform("limit-height", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(20, 10));
    }

    @Test
    void theResolutionLimitNeverUpscales() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(
                new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                new LimitResolution(100, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                JPEG_95.encoding());

        var processed = transform("limit-no-upscale", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(40, 20));
    }

    /** The aspect ratio is applied before the resolution limit: 40x20 pads to 40x40, then scales to 10x10. */
    @Test
    void theAspectRatioIsAppliedBeforeTheResolutionLimit() throws IOException {
        var source = solidJpeg("source", 40, 20, Color.RED);
        var spec = new ImageTransformationSpec(
                new LimitResolution(10, LimitResolution.Mode.KEEP_ASPECT_RATIO),
                null,
                new AspectRatio(1, 1, AspectRatio.Mode.CONTAIN),
                new ImageEncoding.Jpeg(95));

        var processed = transform("ratio-then-limit", source, spec);

        assertThat(processed.metadata()).isEqualTo(new ImageMetadata(10, 10));
    }

    /** The quality is applied: the same noisy image encodes smaller at low quality. */
    @Test
    void jpegQualityChangesTheFileSize() throws IOException {
        var source = noiseJpeg("source", 64, 64);
        var low = transform("quality-low", source,
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(10)));
        var high = transform("quality-high", source,
                new ImageTransformationSpec(null, null, null, new ImageEncoding.Jpeg(95)));

        assertThat(Files.size(low.entryPoint())).isLessThan(Files.size(high.entryPoint()));
    }

    // ---- fixture and assertion helpers ----

    private ImageProcessing.ProcessedFile transform(String reportName, Path source, ImageTransformationSpec spec)
            throws IOException {
        var outputDir = Files.createTempDirectory(tempDir, "out-");
        var processed = processor.process(source, outputDir, spec);
        saveReport(reportName, processed.entryPoint());
        return processed;
    }

    /** Keeps the outputs for visual inspection: the automated replacement of the removed docker test. */
    private void saveReport(String reportName, Path entryPoint) {
        try {
            var target = reportDir.resolve(reportName);
            Files.createDirectories(target);
            Files.copy(entryPoint, target.resolve(entryPoint.getFileName().toString()));
        } catch (IOException e) {
            throw new UncheckedIOException("could not save the transformation report", e);
        }
    }

    private Path solidJpeg(String name, int width, int height, Color color) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (var y = 0; y < height; y++)
            for (var x = 0; x < width; x++)
                image.setRGB(x, y, color.getRGB());
        return writeJpeg(name, image);
    }

    /** The left half is one color, the right half the other; the crop assertions sample both. */
    private Path splitJpeg(String name, int width, int height, Color left, Color right) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (var y = 0; y < height; y++)
            for (var x = 0; x < width; x++)
                image.setRGB(x, y, (x < width / 2 ? left : right).getRGB());
        return writeJpeg(name, image);
    }

    /** Fixed-seed noise: the JPEG quality must visibly change the file size of this one. */
    private Path noiseJpeg(String name, int width, int height) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var random = new Random(42);
        for (var y = 0; y < height; y++)
            for (var x = 0; x < width; x++)
                image.setRGB(x, y, random.nextInt(0xFFFFFF));
        return writeJpeg(name, image);
    }

    private Path writeJpeg(String name, BufferedImage image) throws IOException {
        var file = tempDir.resolve(name + ".jpg");
        assertThat(ImageIO.write(image, "jpg", file.toFile())).isTrue();
        return file;
    }

    /** Asserts one sampled pixel of a JPEG round-trip; the codec shifts colors slightly. */
    private static void assertPixel(int rgb, String expected) {
        var red = (rgb >> 16) & 0xFF;
        var green = (rgb >> 8) & 0xFF;
        var blue = rgb & 0xFF;
        switch (expected) {
            case "red" -> {
                assertThat(red).isGreaterThan(200);
                assertThat(green).isLessThan(60);
                assertThat(blue).isLessThan(60);
            }
            case "blue" -> {
                assertThat(blue).isGreaterThan(200);
                assertThat(red).isLessThan(60);
                assertThat(green).isLessThan(60);
            }
            case "black" -> {
                assertThat(red).isLessThan(60);
                assertThat(green).isLessThan(60);
                assertThat(blue).isLessThan(60);
            }
            default -> throw new IllegalArgumentException("unknown expected color: " + expected);
        }
    }

    private static void assertJpegMagic(Path file) throws IOException {
        var header = Files.readAllBytes(file);
        assertThat(header.length).isGreaterThan(3);
        assertThat(header[0]).isEqualTo((byte) 0xFF);
        assertThat(header[1]).isEqualTo((byte) 0xD8);
    }

    private static void assertWebpMagic(Path file) throws IOException {
        var header = Files.readAllBytes(file);
        assertThat(header.length).isGreaterThan(12);
        assertThat(Arrays.copyOfRange(header, 0, 4)).isEqualTo("RIFF".getBytes());
        assertThat(Arrays.copyOfRange(header, 8, 12)).isEqualTo("WEBP".getBytes());
    }
}
