package com.graydang.app.global.s3.util;

import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import com.graydang.app.global.s3.exception.InvalidFileTypeException;
import com.graydang.app.global.s3.model.ContentType;
import com.graydang.app.global.s3.model.dto.S3File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class S3FileUtil {

    public S3File convert(MultipartFile multipartFile)  {

        // todo 커스텀 예외 생성 후 처리 필요
        log.info("멀티파트파일 content type = {}", multipartFile.getContentType());
        ContentType contentType = getContentType(multipartFile)
                .orElseThrow(() -> new InvalidFileTypeException(BaseResponseStatus.INVALID_FILE_TYPE));

        try {
            return S3File.builder()
                    .filename(createCleanedFileName(multipartFile.getOriginalFilename(), contentType))
                    .contentType(contentType)
                    .contentLength(multipartFile.getSize())
                    .inputStream(multipartFile.getInputStream())
                    .build();
        } catch (Exception e) {
            // todo 커스텀 예외 생성 후 IOexception 잡아서 처리 필요
            throw new IllegalArgumentException("파일읽기에 실패함");
        }
    }

    private Optional<ContentType> getContentType(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        log.info("{}", extension);
        return ContentType.getByExtension(extension);
    }

    private String createCleanedFileName(String fileName, ContentType contentType) {

        String cleanedFileName = fileName.replaceAll("[/\\\\\\s]+", "_");
        return contentType.getFilePath() + createFileName(fileName);
    }

    private String createFileName(String fileName) {
        return UUID.randomUUID() + "_" + fileName;
    }
}
