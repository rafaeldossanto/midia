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

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public String upload(String nomeArmazenado, MultipartFile arquivo) {
        try (InputStream inputStream = arquivo.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nomeArmazenado)
                            .stream(inputStream, arquivo.getSize(), -1)
                            .contentType(arquivo.getContentType())
                            .build()
            );

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(nomeArmazenado)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

            log.info("Arquivo {} enviado ao MinIO com sucesso", nomeArmazenado);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar arquivo ao MinIO: " + e.getMessage(), e);
        }
    }

    public void delete(String nomeArmazenado) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(nomeArmazenado)
                            .build()
            );
            log.info("Arquivo {} removido do MinIO", nomeArmazenado);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover arquivo do MinIO: " + e.getMessage(), e);
        }
    }

    public String getBucket() {
        return bucket;
    }
}
