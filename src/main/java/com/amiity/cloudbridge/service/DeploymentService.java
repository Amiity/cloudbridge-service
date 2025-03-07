package com.amiity.cloudbridge.service;

import com.amiity.cloudbridge.utility.DeploymentUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


@Slf4j
@Service
public class DeploymentService {

    public String buildDockerImage(String dockerFileDir, String imageName) throws IOException, InterruptedException {
        log.info("🚀 Building Docker image: {}", imageName);

        // Docker build command: docker build -t <imageName> <dockerFileDir>
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "build", "-t", imageName, dockerFileDir);
        processBuilder.directory(new File(dockerFileDir));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        logProcessOutput(process);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("❌ Docker build failed with exit code: {}", exitCode);
            throw new RuntimeException("Docker build failed.");
        }

        log.info("✅ Docker image built successfully: {}", imageName);
        return imageName;
    }

    public String runDockerContainer(String imageName, String containerName) throws IOException, InterruptedException {
        log.info("🚀 Checking if Docker container '{}' already exists...", containerName);

        // Check if the container exists
        ProcessBuilder checkContainer = new ProcessBuilder(
                "docker", "ps", "-aq", "-f", "name=" + containerName);
        checkContainer.redirectErrorStream(true);

        Process checkProcess = checkContainer.start();
        String containerId = logProcessOutput(checkProcess).trim();
        checkProcess.waitFor();

        if (!containerId.isEmpty()) {
            log.info("✅ Container '{}' already exists with ID: {}", containerName, containerId);

            // Check if the container is running
            ProcessBuilder checkRunning = new ProcessBuilder(
                    "docker", "ps", "-q", "-f", "name=" + containerName);
            checkRunning.redirectErrorStream(true);

            Process runningProcess = checkRunning.start();
            String runningContainerId = logProcessOutput(runningProcess).trim();
            runningProcess.waitFor();

            if (!runningContainerId.isEmpty()) {
                log.info("🔄 Container '{}' is already running. Reusing it.", containerName);
                return runningContainerId;
            }

            log.info("▶️ Restarting existing container: {}", containerName);
            ProcessBuilder restartContainer = new ProcessBuilder("docker", "start", containerName);
            restartContainer.redirectErrorStream(true);
            Process restartProcess = restartContainer.start();
            restartProcess.waitFor();

            return containerName;
        }

        // Find next available port and start a new container
        int availablePort = DeploymentUtility.findAvailablePort(9091);
        return startNewContainer(imageName, containerName, availablePort);
    }

    private String startNewContainer(String imageName, String containerName, int port) throws IOException, InterruptedException {
        log.info("🚀 Running new Docker container: {} from image: {} on port: {}", containerName, imageName, port);
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "-d", "-p", port + ":9090", "--name", containerName, imageName);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String newContainerId = logProcessOutput(process).trim();
        int exitCode = process.waitFor();

        if (exitCode != 0 || newContainerId.isEmpty()) {
            log.error("❌ Docker container failed to start.");
            throw new RuntimeException("Docker run failed.");
        }

        log.info("✅ Docker container started successfully! Container ID: {}", newContainerId);
        return newContainerId;
    }


    private String logProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
