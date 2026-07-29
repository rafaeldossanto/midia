package com.trisha.midia.service;

import com.trisha.midia.entity.MediaFile;
import com.trisha.midia.exception.ForbiddenException;
import com.trisha.midia.mapper.FileMapper;
import com.trisha.midia.model.dto.response.FileContent;
import com.trisha.midia.model.dto.response.FileResponse;
import com.trisha.midia.model.enums.FileType;
import com.trisha.midia.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final MinioService minioService;
    private final MediaFileRepository repository;

    /** Base publica do proprio servico de Midia, usada para montar a URL do binario. */
    @Value("${midia.public-url}")
    private String publicBaseUrl;

    public FileResponse upload(MultipartFile file, FileType type, String ownerId) {
        log.info("Iniciando upload: {} ({})", file.getOriginalFilename(), type);

        validateFile(file, type);

        String storedName = extractStoredName(file.getOriginalFilename());
        minioService.upload(storedName, file);

        MediaFile entity = FileMapper.toEntity(file, type, storedName, minioService.getBucket(),
                ownerId, publicBaseUrl);
        repository.save(entity);

        log.info("Arquivo salvo com id: {}", entity.getId());
        return FileMapper.toResponse(entity, publicBaseUrl);
    }

    public FileResponse getById(String id) {
        return FileMapper.toResponse(findById(id), publicBaseUrl);
    }

    /**
     * Binario do arquivo, servido pelo proprio servico. E o que sustenta a URL
     * permanente: o bucket fica privado e nada expira.
     */
    public FileContent download(String id) {
        MediaFile file = findById(id);
        return new FileContent(file.getContentType(), file.getSizeBytes(),
                minioService.download(file.getStoredName()));
    }

    public void delete(String id, String userId) {
        MediaFile file = findById(id);

        if (!userId.equals(file.getOwnerId())) {
            throw new ForbiddenException("Voce nao e o dono deste arquivo");
        }

        minioService.delete(file.getStoredName());
        repository.delete(file);
        log.info("Arquivo {} deletado", id);
    }

    private MediaFile findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Arquivo nao encontrado"));
    }

    private void validateFile(MultipartFile file, FileType type) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        String contentType = file.getContentType();
        if (isNull(contentType)) {
            throw new IllegalArgumentException("Tipo do arquivo nao identificado");
        }

        if (FileType.FOTO.equals(type) && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tipo FOTO espera um arquivo de imagem");
        }

        if (FileType.VIDEO.equals(type) && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Tipo VIDEO espera um arquivo de video");
        }
    }

    private String extractStoredName(String originalName) {
        String extension = "";
        if (nonNull(originalName) && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return java.util.UUID.randomUUID() + extension;
    }
}
