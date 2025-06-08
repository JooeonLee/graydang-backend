package com.graydang.app.global.s3.controller;

import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.s3.model.ImagePrefix;
import com.graydang.app.global.s3.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/api")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/image")
    public ResponseEntity<BaseResponse<String>> uploadImage(@RequestPart("file") MultipartFile file) {
        log.info("Uploading image: {}", file.getOriginalFilename());
        String url = imageService.upload(file, ImagePrefix.DEFAULT);

        return ResponseEntity.ok(BaseResponse.success(url));
    }
}
