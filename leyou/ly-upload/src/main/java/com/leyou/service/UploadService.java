package com.leyou.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String upLoadImage(MultipartFile file);
}
