package com.graydang.app.global.s3.util;

import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import com.graydang.app.global.s3.exception.S3FileException;
import com.graydang.app.global.s3.model.ImagePrefix;
import com.graydang.app.global.s3.model.dto.S3File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

    private final S3FileUtil s3FileUtil;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3File uploadMultipartFile(MultipartFile file, ImagePrefix imagePrefix) {
        log.info("S3 파일 업로드 시작. [{} of {}]", file.getOriginalFilename(), file.getContentType());
        return uploadS3File(s3FileUtil.convert(file), imagePrefix);
    }

    public S3File uploadS3File(S3File s3File, ImagePrefix imagePrefix) {
        try {
            String key = imagePrefix.getValue() + s3File.getFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(s3File.getContentType().getMimeType())
                    .contentLength(s3File.getContentLength())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(s3File.getInputStream(), s3File.getContentLength()));
            log.info("S3 파일 업로드 완료. [{}]", key);
            return s3File.putObjectUrl(getUrl(key));
        } catch (Exception e) {
            e.printStackTrace();

            throw new S3FileException(BaseResponseStatus.S3_UPLOAD_FAILED);
        }
    }

    private String getUrl(String filename) {
        try {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .build();
            return s3Client.utilities().getUrl(getUrlRequest).toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new S3FileException(BaseResponseStatus.S3_URL_GENERATION_FAILED);
        }
    }

    public void delete(String objectUrl) {
        S3Uri s3Uri = s3Client.utilities().parseUri(URI.create(objectUrl));
        // todo 커스텀 예외 생성
        String filename = s3Uri.key()
                .orElseThrow(() -> new S3FileException(BaseResponseStatus.INVALID_S3_URI));
        deleteObject(filename);
    }

    public void deleteObject(String filename) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();

            throw new S3FileException(BaseResponseStatus.S3_FILE_DELETE_FAILED);
        }
    }
}
