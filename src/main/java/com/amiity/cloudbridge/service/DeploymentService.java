package com.amiity.cloudbridge.service;

import com.amiity.cloudbridge.dto.DeploymentRequest;
import com.amiity.cloudbridge.utility.DeploymentUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


@Slf4j
@Service
public class DeploymentService {


    @Autowired
    SavePackageService savePackageService;


    public String deploymentLabs(DeploymentRequest deploymentRequest) throws IOException, InterruptedException {

        String imageName = deploymentRequest.getImageName().toLowerCase();
        File copiedDockerFile = savePackageService.downloadPackage(deploymentRequest.getDeploymentFile(), imageName, deploymentRequest.getDockerFile());
        String img = buildDockerImage(copiedDockerFile.getParent(), imageName);
        return runDockerContainer(img, imageName, deploymentRequest.getServerPort());
    }


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

    public String runDockerContainer(String imageName, String containerName, String containerPort) throws IOException, InterruptedException {
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
                log.info("🔄 Container '{}' is already running. Removing it.", containerName);
                // Stop and remove the old container
                new ProcessBuilder("docker", "stop", containerName).start().waitFor();
                new ProcessBuilder("docker", "rm", containerName).start().waitFor();
                //return getContainerHostPort(containerName);
            }

            /** This feature will be enabled for running containers that have stopped due to rental timeout
             *
            log.info("▶️ Restarting existing container: {}", containerName);
            ProcessBuilder restartContainer = new ProcessBuilder("docker", "start", containerName);
            restartContainer.redirectErrorStream(true);
            Process restartProcess = restartContainer.start();
            restartProcess.waitFor();
            */

            //return getContainerHostPort(containerName);
        }

        // Find next available port and start a new container
        int availablePort = DeploymentUtility.findAvailablePort();
        return startNewContainer(imageName, containerName, availablePort, containerPort);
    }

    private String startNewContainer(String imageName, String containerName, int port, String containerPort) throws IOException, InterruptedException {
        log.info("🚀 Running new Docker container: {} from image: {} on port: {}", containerName, imageName, port);
        int containerPortNumber = Integer.parseInt(containerPort);
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "-d", "-p", port + ":" + containerPortNumber, "--name", containerName, imageName);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String newContainerId = logProcessOutput(process).trim();
        int exitCode = process.waitFor();

        if (exitCode != 0 || newContainerId.isEmpty()) {
            log.error("❌ Docker container failed to start.");
            throw new RuntimeException("Docker run failed.");
        }

        log.info("✅ Docker container started successfully! Container ID: {}", newContainerId);
        return String.valueOf(port);
    }

    private String getContainerHostPort(String containerName) throws IOException, InterruptedException {
        ProcessBuilder inspect = new ProcessBuilder(
                "docker", "inspect", "--format",
                "{{range $key, $value := .NetworkSettings.Ports}} {{(index $value 0).HostPort}} {{end}}",
                containerName
        );
        inspect.redirectErrorStream(true);
        Process process = inspect.start();
        String port = logProcessOutput(process).trim();
        process.waitFor();

        if (port.isEmpty()) {
            log.error("❌ No port mapping found for container: {}", containerName);
            throw new RuntimeException("Port not found for container: " + containerName);
        }

        log.info("🔌 Host port for container '{}': {}", containerName, port);
        return port;
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
