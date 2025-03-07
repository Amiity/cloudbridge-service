package com.amiity.cloudbridge.controller;

import com.amiity.cloudbridge.dto.DeploymentRequest;
import com.amiity.cloudbridge.service.DeploymentService;
import com.amiity.cloudbridge.service.FileUploadService;
import com.amiity.cloudbridge.service.SavePackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api")
public class DeploymentController {


    @Autowired
    DeploymentService deploymentService;

    @PostMapping("/deploy")
    public ResponseEntity<String> uploadDeploymentFile(
            @RequestParam("deploymentFile") MultipartFile deploymentFile,
            @RequestParam("serverPort") String serverPort,
            @RequestParam("dockerFile") MultipartFile dockerFile,
            @RequestParam("deploymentType") String deploymentType,
            @RequestParam("imageName") String imageName) throws IOException, InterruptedException {

        DeploymentRequest deploymentRequest = new DeploymentRequest();
        deploymentRequest.setDeploymentFile(deploymentFile);
        deploymentRequest.setServerPort(serverPort);
        deploymentRequest.setDockerFile(dockerFile);
        deploymentRequest.setDeploymentType(deploymentType);
        deploymentRequest.setImageName(imageName);

        if (deploymentFile.isEmpty() || dockerFile.isEmpty()) {
            return ResponseEntity.badRequest().body("File must not be empty");
        }

        String hostPort = deploymentService.deploymentLabs(deploymentRequest);
        return ResponseEntity.ok("File uploaded successfully! hostPort : " + hostPort);
    }
}


