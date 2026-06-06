package com.trisha.midia.controller;

import com.trisha.midia.model.dto.response.ArquivoResponse;
import com.trisha.midia.model.enums.TipoArquivo;
import com.trisha.midia.service.ArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/arquivo")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService arquivoService;

    @PostMapping("/upload")
    public ArquivoResponse upload(@RequestParam("arquivo") MultipartFile arquivo,
                                  @RequestParam("tipo") TipoArquivo tipo) {
        return arquivoService.upload(arquivo, tipo);
    }

    @GetMapping("/{id}")
    public ArquivoResponse getById(@PathVariable String id) {
        return arquivoService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        arquivoService.delete(id);
    }
}
