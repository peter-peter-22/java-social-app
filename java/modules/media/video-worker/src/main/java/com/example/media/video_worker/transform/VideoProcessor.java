package com.example.media.video_worker.transform;

import com.example.media.contract.asset.VideoMetadata;
import com.example.media.contract.transformation.VideoEncoding;
import com.example.media.contract.transformation.VideoTransformationSpec;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.HlsVariant;
import net.bramp.ffmpeg.builder.Strict;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class VideoProcessor implements VideoProcessing {
    private final FFprobe ffprobe;

    {
        try {
            ffprobe = new FFprobe();
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing FFmpeg:", e);
        }
    }

    /**
     * Read the video metadata with ffprobe.
     */
    @Override
    public VideoMetadata readMetadata(Path file) {
        try {

            FFmpegProbeResult probeResult = ffprobe.probe(file.toAbsolutePath().toString());

            FFmpegFormat format = probeResult.getFormat();
            FFmpegStream stream = probeResult.getStreams().getFirst();

            return new VideoMetadata(
                    stream.getWidth(),
                    stream.getHeight(),
                    (long) format.getDuration()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract video metadata:", e);
        }
    }

    /**
     * Create the output files of a video transformation.
     * Return the entrypoint and the metadata.
     */
    @Override
    public ProcessedFile process(Path source, Path outputDir, VideoTransformationSpec spec) {
        // input
        var builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(source.toAbsolutePath())
                .done();

        // encoding
        var entryPoint = applyEncoding(builder, outputDir, spec.encoding());

        // process the job
        try {
            new FFmpegExecutor().createJob(builder).run();
        } catch (Exception e) {
            throw new RuntimeException("Error while executing the FFmpeg job:", e);
        }

        // get the output metadata
        // TODO verify if it the probe can read HLS playlists
        // TODO HLS has multiple resolutions, so the metadata cannot work.
        //  The goal of the metadata is to expose the media geometry to the main application that will forward it to the browser client.
        //  The browser will size the image/video elements based on the metadata.
        //  Storing the largest resolution in the metadata should solve this issue.
        var metadata = readMetadata(entryPoint);
        return new ProcessedFile(entryPoint, metadata);
    }

    private Path applyEncoding(FFmpegBuilder fFmpegBuilder, Path outputDir, VideoEncoding encoding) {
        return switch (encoding) {
            case VideoEncoding.Mp4 mp4Encoding -> {
                var entryPoint = outputDir.resolve("result.mp4");
                var builder = fFmpegBuilder.addOutput(entryPoint.toAbsolutePath());

                if (mp4Encoding.targetSizeBytes() != null)
                    builder.setTargetSize(mp4Encoding.targetSizeBytes());
                if (mp4Encoding.disableSubtitles() != null)
                    builder.disableSubtitle();
                if (mp4Encoding.audioChannels() != null)
                    builder.setAudioChannels(mp4Encoding.audioChannels());
                if (mp4Encoding.audioCodec() != null)
                    builder.setAudioCodec(mp4Encoding.audioCodec().name());
                if (mp4Encoding.audioSampleRateHz() != null)
                    builder.setAudioSampleRate(mp4Encoding.audioSampleRateHz());
                if (mp4Encoding.audioBitrate() != null)
                    builder.setAudioBitRate(mp4Encoding.audioBitrate());
                if (mp4Encoding.videoFrameRate() != null)
                    builder.setVideoFrameRate(mp4Encoding.videoFrameRate(), 1);
                if (mp4Encoding.limitResolution() != null)
                    IO.println("not yet implemented"); // TODO: investigate how ffmpeg can limit the resolution while keeping the aspect ratio
                if (mp4Encoding.allowExperimental() != null)
                    builder.setStrict(Strict.EXPERIMENTAL);
                if (mp4Encoding.constantRateFactor() != null)
                    builder.setConstantRateFactor(mp4Encoding.constantRateFactor());
                if (mp4Encoding.encodingPreset() != null)
                    builder.setPreset(mp4Encoding.encodingPreset().name());
                if (mp4Encoding.videoBitrate() != null)
                    builder.setVideoBitRate(mp4Encoding.videoBitrate());
                if (mp4Encoding.disableAudio() != null)
                    builder.disableAudio();

                yield entryPoint;
            }
            case VideoEncoding.Hls hlsEncoding -> {
                var variantDir = outputDir.resolve("var-%v");
                var masterPlaylistName = "master.m3u8";
                var builder = fFmpegBuilder.addHlsOutput(variantDir.resolve("index.m3u8").toAbsolutePath());

                builder = builder.setHlsTime(10, TimeUnit.SECONDS)
                        .addExtraArgs("-hls_segment_type", "fmp4")
                        .setHlsPlaylistType("vod")
                        .setHlsListSize(0) // Max number of the segments per track. 0 means unlimited.
                        .setHlsSegmentFileName(variantDir.resolve("segment-%d.m4s").toAbsolutePath().toString())
                        .setMasterPlName(masterPlaylistName)
                        .setPreset("slow")
                        .addExtraArgs("-g", "60") // group of pictures, should read from the encoding
                        .addExtraArgs("-sc_threshold", "0");

                // Map the same input multiple times for different variants

                if (hlsEncoding.tracks() != null) {
                    for (int i = 0; i < hlsEncoding.tracks().size(); i++) {
                        var track = hlsEncoding.tracks().get(i);

                        builder.addVariant(new HlsVariant().addVideo(i).addAudio(i).setName(track.name()))
                                .addExtraArgs("-map", "0:0", "-map", "0:1"); // create the track then set the input

                        if (track.videoBitrate() != null)
                            builder.addExtraArgs("-b:v:"+i, track.videoBitrate().toString());
                        if (track.audioBitrate() != null)
                            builder.addExtraArgs("-b:a:"+i, track.audioBitrate().toString());
                        if (track.constantRateFactor() != null)
                            builder.addExtraArgs("-crf:v:"+i, track.constantRateFactor().toString());
                    }
                }

                builder.setAudioCodec("copy")
                        .done();

                yield outputDir.resolve(masterPlaylistName);
            }
        };
    }


}
