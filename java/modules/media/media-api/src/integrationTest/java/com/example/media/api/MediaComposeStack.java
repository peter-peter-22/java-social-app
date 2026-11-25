package com.example.media.api;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Boots the compose stack of {@code modules/media/deployment} for the end-to-end test: the image
 * worker (running the jar built by the {@code integrationTest} task), the database, the object
 * storage, and RabbitMQ. Local compose mode uses the same {@code compose.yaml} the development
 * workflow runs, so the test and the dev stack cannot drift apart. The media API itself stays
 * in-process in the test; the worker reaches it through the host gateway like in development.
 *
 * <p>{@code start} must run before the Spring context boots (the context runs the registry
 * migrations), so the test class calls it from a static initializer. The containers carry the
 * Testcontainers labels, so Ryuk reaps them when the test JVM exits.</p>
 */
final class MediaComposeStack {

    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(5);
    private static ComposeContainer compose;

    private MediaComposeStack() {
    }

    static synchronized void start() {
        if (compose != null)
            return;
        assertHostPortsFree();
        var composeFile = Path.of("..", "deployment", "compose.yaml").toFile();
        if (!composeFile.exists())
            throw new IllegalStateException(
                    "compose file not found: " + composeFile.getAbsolutePath()
                            + " — the test runs with the media-api module directory as the working directory");
        // The file-only constructor runs the locally installed docker compose, so the test uses the
        // same compose.yaml and the same host-port workflow as the development stack.
        compose = new ComposeContainer(composeFile)
                // The compose file drops the container to the host user through these ids.
                .withEnv("HOST_UID", hostId("-u"))
                .withEnv("HOST_GID", hostId("-g"))
                .withStartupTimeout(STARTUP_TIMEOUT)
                .withExposedService("db", 26257, Wait.forListeningPort())
                .withExposedService("minio", 9000, Wait.forListeningPort())
                .withExposedService("rabbitmq", 5672, Wait.forListeningPort())
                .withExposedService("image-worker", 8092, Wait.forListeningPort());
        compose.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> compose.stop()));
    }

    /** The host user ids the compose file interpolates; {@code id} is the portable way to read them. */
    private static String hostId(String flag) {
        try {
            var process = new ProcessBuilder("id", flag).start();
            var value = new String(process.getInputStream().readAllBytes()).trim();
            if (process.waitFor() != 0)
                throw new IllegalStateException("id " + flag + " failed");
            return value;
        } catch (IOException e) {
            throw new IllegalStateException("could not read the host user id", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while reading the host user id", e);
        }
    }

    /**
     * The compose file publishes fixed host ports, so the test's stack cannot share them with the
     * development stack. Fail fast with the fix instead of a cryptic compose error.
     */
    private static void assertHostPortsFree() {
        for (var port : new int[]{26257, 5672, 9000, 8082}) {
            try (var socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", port), 250);
            } catch (IOException e) {
                continue; // nothing listens: the port is free
            }
            throw new IllegalStateException(
                    "port " + port + " is in use — stop the development stack first"
                            + " (modules/media/deployment/run.bash down); the end-to-end test boots"
                            + " its own compose stack on the fixed host ports");
        }
    }
}
