package com.trisha.midia.stub;

import com.trisha.midia.entity.MediaFile;
import com.trisha.midia.model.enums.FileType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Facilitador de testes para MediaFile e MultipartFile.
 */
public final class FileStub {

    public static final String ID = "arquivo-1";
    public static final String BUCKET = "trilha-midia";
    public static final String URL = "https://minio/trilha-midia/arquivo.jpg?presigned";
    public static final String OWNER_ID = "usuario-1";

    private FileStub() {
    }

    public static MediaFile.MediaFileBuilder aFile() {
        return MediaFile.builder()
                .id(ID)
                .originalName("foto.jpg")
                .storedName("uuid-gerado.jpg")
                .type(FileType.FOTO)
                .contentType("image/jpeg")
                .sizeBytes(1024L)
                .bucket(BUCKET)
                .url(URL)
                .ownerId(OWNER_ID)
                .createdAt(LocalDateTime.now());
    }

    /** MultipartFile de imagem valida. */
    public static MultipartFile aPhoto() {
        return new MockMultipartFile(
                "arquivo", "foto.jpg", "image/jpeg", "conteudo-imagem".getBytes());
    }

    /** MultipartFile de video valido. */
    public static MultipartFile aVideo() {
        return new MockMultipartFile(
                "arquivo", "video.mp4", "video/mp4", "conteudo-video".getBytes());
    }

    /** MultipartFile vazio. */
    public static MultipartFile anEmptyFile() {
        return new MockMultipartFile(
                "arquivo", "vazio.jpg", "image/jpeg", new byte[0]);
    }

    /** MultipartFile sem content-type. */
    public static MultipartFile aFileWithoutContentType() {
        return new MockMultipartFile(
                "arquivo", "arquivo.bin", null, "conteudo".getBytes());
    }
}
