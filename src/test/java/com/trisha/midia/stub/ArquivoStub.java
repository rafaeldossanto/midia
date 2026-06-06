package com.trisha.midia.stub;

import com.trisha.midia.entity.ArquivoMidia;
import com.trisha.midia.model.enums.TipoArquivo;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Facilitador de testes para ArquivoMidia e MultipartFile.
 */
public final class ArquivoStub {

    public static final String ID = "arquivo-1";
    public static final String BUCKET = "trilha-midia";
    public static final String URL = "https://minio/trilha-midia/arquivo.jpg?presigned";

    private ArquivoStub() {
    }

    public static ArquivoMidia.ArquivoMidiaBuilder umArquivo() {
        return ArquivoMidia.builder()
                .id(ID)
                .nomeOriginal("foto.jpg")
                .nomeArmazenado("uuid-gerado.jpg")
                .tipo(TipoArquivo.FOTO)
                .contentType("image/jpeg")
                .tamanhoBytes(1024L)
                .bucket(BUCKET)
                .url(URL)
                .criadoEm(LocalDateTime.now());
    }

    /** MultipartFile de imagem valida. */
    public static MultipartFile umaFoto() {
        return new MockMultipartFile(
                "arquivo", "foto.jpg", "image/jpeg", "conteudo-imagem".getBytes());
    }

    /** MultipartFile de video valido. */
    public static MultipartFile umVideo() {
        return new MockMultipartFile(
                "arquivo", "video.mp4", "video/mp4", "conteudo-video".getBytes());
    }

    /** MultipartFile vazio. */
    public static MultipartFile umArquivoVazio() {
        return new MockMultipartFile(
                "arquivo", "vazio.jpg", "image/jpeg", new byte[0]);
    }

    /** MultipartFile sem content-type. */
    public static MultipartFile umArquivoSemContentType() {
        return new MockMultipartFile(
                "arquivo", "arquivo.bin", null, "conteudo".getBytes());
    }
}
