package com.trisha.midia.service;

import com.trisha.midia.entity.ArquivoMidia;
import com.trisha.midia.mapper.ArquivoMapper;
import com.trisha.midia.model.dto.response.ArquivoResponse;
import com.trisha.midia.model.enums.TipoArquivo;
import com.trisha.midia.repository.ArquivoMidiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArquivoService {

    private final MinioService minioService;
    private final ArquivoMidiaRepository repository;

    public ArquivoResponse upload(MultipartFile arquivo, TipoArquivo tipo) {
        log.info("Iniciando upload: {} ({})", arquivo.getOriginalFilename(), tipo);

        validarArquivo(arquivo, tipo);

        String nomeArmazenado = extrairNomeArmazenado(arquivo.getOriginalFilename());
        String url = minioService.upload(nomeArmazenado, arquivo);

        ArquivoMidia entidade = ArquivoMapper.toEntity(arquivo, tipo, nomeArmazenado, url, minioService.getBucket());
        repository.save(entidade);

        log.info("Arquivo salvo com id: {}", entidade.getId());
        return ArquivoMapper.toResponse(entidade);
    }

    public ArquivoResponse getById(String id) {
        return ArquivoMapper.toResponse(findById(id));
    }

    public void delete(String id) {
        ArquivoMidia arquivo = findById(id);
        minioService.delete(arquivo.getNomeArmazenado());
        repository.delete(arquivo);
        log.info("Arquivo {} deletado", id);
    }

    private ArquivoMidia findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Arquivo nao encontrado"));
    }

    private void validarArquivo(MultipartFile arquivo, TipoArquivo tipo) {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        String contentType = arquivo.getContentType();
        if (isNull(contentType)) {
            throw new IllegalArgumentException("Tipo do arquivo nao identificado");
        }

        if (TipoArquivo.FOTO.equals(tipo) && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Tipo FOTO espera um arquivo de imagem");
        }

        if (TipoArquivo.VIDEO.equals(tipo) && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Tipo VIDEO espera um arquivo de video");
        }
    }

    private String extrairNomeArmazenado(String nomeOriginal) {
        String extensao = "";
        if (nonNull(nomeOriginal) && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return java.util