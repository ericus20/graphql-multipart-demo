package com.example.controller;

import com.example.response.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Controller
public class FileUploadController {

    @MutationMapping
    public FileUploadResult fileUpload(@Argument MultipartFile file) {
        log.info("Upload file: name={}", file.getOriginalFilename());

        return new FileUploadResult(UUID.randomUUID());
    }
}
