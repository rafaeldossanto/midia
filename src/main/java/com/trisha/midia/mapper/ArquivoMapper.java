package com.trisha.midia.mapper;

import com.trisha.midia.entity.ArquivoMidia;
import com.trisha.midia.model.dto.response.ArquivoResponse;
import com.trisha.midia.model.enums.TipoArquivo;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class ArquivoMapper {

    public static ArquivoMidia toEntity(MultipartFile arquivo, TipoArquivo tipo, String nomeArmazenado,
                                        String url, String bucket, String proprietarioId) {
        return ArquivoMidia.builder()
                .id(UUID.randomUUID().toString())
                .nomeOriginal(arquivo.getOriginalFilename())
                .nomeArmazenado(nomeArmazenado)
                .tipo(tipo)
                .contentType(arquivo.getContentType())
                .tamanhoBytes(arquivo.getSize())
                .bucket(bucket)
                .url(url)
                .proprietarioId(proprietarioId)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    public static ArquivoResponse toResponse(ArquivoMidia entity) {
        return ArquivoResponse.builder()
                .id(entity.getId())
                .nomeOriginal(entity.getNomeOriginal())
                .tipo(entity.getTipo())
                .url(entity.getUrl())
                .tamanhoBytes(entity.getTamanhoBytes())
                .criadoEm(entity.getCriadoEm())
                .build();
    }
}
