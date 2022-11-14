package com.example.request;

import lombok.Data;
import lombok.NonNull;

@Data
public class FileUploadRequest {
    @NonNull private User user;
}
