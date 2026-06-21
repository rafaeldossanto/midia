package com.trisha.midia.mapper;

import com.trisha.midia.entity.MediaFile;
import com.trisha.midia.model.dto.response.FileResponse;
import com.trisha.midia.model.enums.FileType;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class FileMapper {

    public static MediaFile toEntity(MultipartFile file, FileType type, String storedName,
                                     String url, String bucket, String ownerId) {
        return MediaFile.builder()
                .id(UUID.randomUUID().toString())
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .type(type)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .bucket(bucket)
                .url(url)
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static FileResponse toResponse(MediaFile entity) {
        return FileResponse.builder()
                .id(entity.getId())
                .originalName(entity.getOriginalName())
                .type(entity.getType())
                .url(entity.getUrl())
                .sizeBytes(entity.getSizeBytes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
