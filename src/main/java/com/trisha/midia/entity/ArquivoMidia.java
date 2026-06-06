package com.trisha.midia.entity;

import com.trisha.midia.model.enums.TipoArquivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "arquivo_midia")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoMidia {

    @Id
    private String id;

    @Column(nullable = false)
    private String nomeOriginal;

    @Column(nullable = false)
    private String nomeArmazenado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoArquivo tipo;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long tamanhoBytes;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDateTime criadoEm;
}
