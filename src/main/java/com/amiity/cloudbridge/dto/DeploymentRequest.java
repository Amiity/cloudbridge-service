package com.amiity.cloudbridge.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DeploymentRequest {

    private MultipartFile deploymentFile;
    private String serverPort;
    private MultipartFile dockerFile;
    private String deploymentType;
    private String imageName;

}
