package com.amiity.cloudbridge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@Slf4j
@Service
public class SavePackageService {

    private static final String DEPLOYMENT_DIR = "/tmp/deployments";

    public File downloadPackage(MultipartFile file, String imageName, MultipartFile dockerfile) throws IOException {

        File copiedDockerfile = copyDockerfile(imageName, dockerfile);

        // Determine the target directory from docker file where the JAR file should be copy
        String jarDestination = determineJarDestination(dockerfile);

        // Construct the directory path based on the extracted destination
        File dir = new File(DEPLOYMENT_DIR, imageName + File.separator + jarDestination);

        // Ensure the directory exists
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("❌ Failed to create directory: " + dir.getAbsolutePath());
        }

        // Save the file in the determined directory
        saveFile(file, dir);

        return copiedDockerfile;
    }

    private File copyDockerfile(String imageName, MultipartFile dockerfile) throws IOException {
        // Save the Dockerfile in the imageName directory
        File dockerDir = new File(DEPLOYMENT_DIR, imageName);
        if (!dockerDir.exists() && !dockerDir.mkdirs()) {
            throw new IOException("❌ Failed to create Dockerfile directory: " + dockerDir.getAbsolutePath());
        }
        return saveFile(dockerfile, dockerDir);
    }

    private String determineJarDestination(MultipartFile dockerfile) throws IOException {
        String defaultJarDestination = "";
        ; // Default to "target" unless overridden

        if (dockerfile != null && !dockerfile.isEmpty()) {
            String dockerfileContent = new String(dockerfile.getBytes());
            String extractedPath = extractCopySource(dockerfileContent);

            if (extractedPath != null && !extractedPath.isEmpty()) {
                return extractedPath;
            }
        }
        return defaultJarDestination;
    }

    private File saveFile(MultipartFile file, File dir) throws IOException {
        File tempFile = new File(dir, file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        if (!tempFile.exists()) {
            throw new IOException("❌ File was not saved: " + tempFile.getAbsolutePath());
        }

        log.info("✅ Saved file: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    private String extractCopySource(String dockerfileContent) {
        String[] lines = dockerfileContent.split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("COPY")) {
                String[] parts = line.split("\\s+");

                // Ensure the COPY instruction has at least "COPY <source> <destination>"
                if (parts.length >= 3 && parts[1].contains("*.jar")) {
                    return parts[1].replace("*.jar", "").replaceAll("/$", ""); // Extract path before *.jar, remove trailing slash
                }
            }
        }
        return null; // No valid COPY instruction found
    }

}
