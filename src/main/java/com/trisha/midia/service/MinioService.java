package com.trisha.midia.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

 //todo - retirar os try catchs daqui para mostrarem o erro real quando der e nao mascarar, caso possivel.

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public String upload(String storedName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

            log.info("Arquivo {} enviado ao MinIO com sucesso", storedName);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar arquivo ao MinIO: " + e.getMessage(), e);
        }
    }

    public void delete(String storedName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .build()
            );
            log.info("Arquivo {} removido do MinIO", storedName);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover arquivo do MinIO: " + e.getMessage(), e);
        }
    }

    public String getBucket() {
        return bucket;
    }
}
