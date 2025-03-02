package com.amiity.cloudbridge.controller;

import com.amiity.cloudbridge.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    FileUploadService fileUploadService;

    @PostMapping("/jar")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Trigger File to S3");
        return fileUploadService.uploadFile(file);
    }

}
