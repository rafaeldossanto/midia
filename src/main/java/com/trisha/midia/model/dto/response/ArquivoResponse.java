package com.trisha.midia.model.dto.response;

import com.trisha.midia.model.enums.TipoArquivo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ArquivoResponse(
        String id,
        String nomeOriginal,
        TipoArquivo tipo,
        String url,
        Long tamanhoBytes,
        LocalDateTime criadoEm
) {}
