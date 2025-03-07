package com.amiity.cloudbridge.controller;

import com.amiity.cloudbridge.service.DeploymentService;
import com.amiity.cloudbridge.service.FileUploadService;
import com.amiity.cloudbridge.service.SavePackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    SavePackageService savePackageService;

    @PostMapping("/jar")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("containerName") String containerName, @RequestParam("dockerFile") MultipartFile dockerFile) throws IOException, InterruptedException {

        /*  this feature is deprecated
        log.info("Trigger File to S3");
        String s3Url = fileUploadService.uploadFile(file);
        */

        String imageName = file.getOriginalFilename().replace(".jar", "").toLowerCase();
        log.info(imageName);


        File tempFile = savePackageService.saveTempFile(file, imageName, dockerFile);
        log.info(tempFile.getParent());
        String img = deploymentService.buildDockerImage(tempFile.getParent(), imageName);
        String containerId = deploymentService.runDockerContainer(img, containerName);
        return "Uploaded & Deployed! Container ID: " + containerId + "\nS3 Backup URL: " + "temp";
    }
}
