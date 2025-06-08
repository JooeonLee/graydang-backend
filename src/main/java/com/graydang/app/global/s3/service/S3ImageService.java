package com.graydang.app.global.s3.service;

import com.graydang.app.global.s3.model.ImagePrefix;
import com.graydang.app.global.s3.model.dto.S3File;
import com.graydang.app.global.s3.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

    private final S3Util s3Util;

    @Override
    public String upload(MultipartFile file, ImagePrefix imagePrefix) {

        S3File s3File = s3Util.uploadMultipartFile(file, imagePrefix);
        return s3File.getObjectUrl();
    }
}
