package com.graydang.app.global.s3.service;

import com.graydang.app.global.s3.model.ImagePrefix;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String upload(MultipartFile file, ImagePrefix imagePrefix);
}
