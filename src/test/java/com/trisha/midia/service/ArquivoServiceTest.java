package com.trisha.midia.service;

import com.trisha.midia.entity.ArquivoMidia;
import com.trisha.midia.model.dto.response.ArquivoResponse;
import com.trisha.midia.model.enums.TipoArquivo;
import com.trisha.midia.repository.ArquivoMidiaRepository;
import com.trisha.midia.stub.ArquivoStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArquivoService")
class ArquivoServiceTest {

    @Mock
    private MinioService minioService;
    @Mock
    private ArquivoMidiaRepository repository;

    @InjectMocks
    private ArquivoService service;

    @Test
    @DisplayName("upload deve validar, enviar ao MinIO e persistir metadados com o dono")
    void deveFazerUpload() {
        MultipartFile foto = ArquivoStub.umaFoto();
        when(minioService.upload(anyString(), any(MultipartFile.class))).thenReturn(ArquivoStub.URL);
        when(minioService.getBucket()).thenReturn(ArquivoStub.BUCKET);
        when(repository.save(any(ArquivoMidia.class))).thenAnswer(inv -> inv.getArgument(0));

        ArquivoResponse response = service.upload(foto, TipoArquivo.FOTO, ArquivoStub.PROPRIETARIO_ID);

        assertThat(response.url()).isEqualTo(ArquivoStub.URL);
        assertThat(response.tipo()).isEqualTo(TipoArquivo.FOTO);
        assertThat(response.nomeOriginal()).isEqualTo("foto.jpg");
        verify(minioService).upload(anyString(), any(MultipartFile.class));
        verify(repository).save(any(ArquivoMidia.class));
    }

    @Test
    @DisplayName("upload deve aceitar video com content-type de video")
    void deveAceitarVideo() {
        MultipartFile video = ArquivoStub.umVideo();
        when(minioService.upload(anyString(), any(MultipartFile.class))).thenReturn(ArquivoStub.URL);
        when(minioService.getBucket()).thenReturn(ArquivoStub.BUCKET);
        when(repository.save(any(ArquivoMidia.class))).thenAnswer(inv -> inv.getArgument(0));

        ArquivoResponse response = service.upload(video, TipoArquivo.VIDEO, ArquivoStub.PROPRIETARIO_ID);

        assertThat(response.tipo()).isEqualTo(TipoArquivo.VIDEO);
    }

    @Test
    @DisplayName("upload deve falhar com arquivo vazio")
    void deveFalharArquivoVazio() {
        assertThatThrownBy(() -> service.upload(ArquivoStub.umArquivoVazio(), TipoArquivo.FOTO, ArquivoStub.PROPRIETARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo vazio");

        verify(minioService, never()).upload(anyString(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("upload deve falhar quando content-type nao identificado")
    void deveFalharSemContentType() {
        assertThatThrownBy(() -> service.upload(ArquivoStub.umArquivoSemContentType(), TipoArquivo.FOTO, ArquivoStub.PROPRIETARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo do arquivo nao identificado");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("upload deve falhar quando FOTO recebe arquivo que nao e imagem")
    void deveFalharFotoComTipoErrado() {
        assertThatThrownBy(() -> service.upload(ArquivoStub.umVideo(), TipoArquivo.FOTO, ArquivoStub.PROPRIETARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo FOTO espera um arquivo de imagem");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("upload deve falhar quando VIDEO recebe arquivo que nao e video")
    void deveFalharVideoComTipoErrado() {
        assertThatThrownBy(() -> service.upload(ArquivoStub.umaFoto(), TipoArquivo.VIDEO, ArquivoStub.PROPRIETARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo VIDEO espera um arquivo de video");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("getById deve retornar arquivo existente")
    void deveRetornarPorId() {
        when(repository.findById(ArquivoStub.ID)).thenReturn(Optional.of(ArquivoStub.umArquivo().build()));

        ArquivoResponse response = service.getById(ArquivoStub.ID);

        assertThat(response.id()).isEqualTo(ArquivoStub.ID);
    }

    @Test
    @DisplayName("getById deve falhar quando arquivo nao existe")
    void deveFalharGetByIdInexistente() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo nao encontrado");
    }

    @Test
    @DisplayName("delete deve remover do MinIO e do banco quando e o dono")
    void deveDeletar() {
        ArquivoMidia arquivo = ArquivoStub.umArquivo().build();
        when(repository.findById(ArquivoStub.ID)).thenReturn(Optional.of(arquivo));

        service.delete(ArquivoStub.ID, ArquivoStub.PROPRIETARIO_ID);

        verify(minioService).delete(arquivo.getNomeArmazenado());
        verify(repository).delete(arquivo);
    }

    @Test
    @DisplayName("delete deve falhar quando nao e o dono")
    void deveFalharDeletarNaoDono() {
        ArquivoMidia arquivo = ArquivoStub.umArquivo().build();
        when(repository.findById(ArquivoStub.ID)).thenReturn(Optional.of(arquivo));

        assertThatThrownBy(() -> service.delete(ArquivoStub.ID, "outro-usuario"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao e o dono");

        verify(minioService, never()).delete(anyString());
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete deve falhar quando arquivo nao existe")
    void deveFalharDeletarInexistente() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("inexistente", ArquivoStub.PROPRIETARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo nao encontrado");

        verify(minioService, never()).delete(anyString());
    }
}
