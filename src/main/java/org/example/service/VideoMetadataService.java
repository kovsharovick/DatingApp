package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoMetadataService {

    public int getDurationSec(Path videoPath) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output == null || output.isBlank()) {
                throw new RuntimeException("ffprobe failed, exit code " + exitCode);
            }
            double duration = Double.parseDouble(output.trim());
            return (int) Math.ceil(duration);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to get video duration", e);
            throw new RuntimeException("Cannot determine video duration", e);
        }
    }
}