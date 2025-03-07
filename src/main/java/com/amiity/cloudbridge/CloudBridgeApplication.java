package com.amiity.cloudbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBridgeApplication.class, args);
        System.out.println("Temp Directory: " + System.getProperty("java.io.tmpdir"));
    }

}
