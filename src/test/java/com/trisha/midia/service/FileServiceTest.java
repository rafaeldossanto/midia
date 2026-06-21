package com.trisha.midia.service;

import com.trisha.midia.entity.MediaFile;
import com.trisha.midia.model.dto.response.FileResponse;
import com.trisha.midia.model.enums.FileType;
import com.trisha.midia.repository.MediaFileRepository;
import com.trisha.midia.stub.FileStub;
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
@DisplayName("FileService")
class FileServiceTest {

    @Mock
    private MinioService minioService;
    @Mock
    private MediaFileRepository repository;

    @InjectMocks
    private FileService service;

    @Test
    @DisplayName("upload deve validar, enviar ao MinIO e persistir metadados com o dono")
    void shouldUpload() {
        MultipartFile photo = FileStub.aPhoto();
        when(minioService.upload(anyString(), any(MultipartFile.class))).thenReturn(FileStub.URL);
        when(minioService.getBucket()).thenReturn(FileStub.BUCKET);
        when(repository.save(any(MediaFile.class))).thenAnswer(inv -> inv.getArgument(0));

        FileResponse response = service.upload(photo, FileType.FOTO, FileStub.OWNER_ID);

        assertThat(response.url()).isEqualTo(FileStub.URL);
        assertThat(response.type()).isEqualTo(FileType.FOTO);
        assertThat(response.originalName()).isEqualTo("foto.jpg");
        verify(minioService).upload(anyString(), any(MultipartFile.class));
        verify(repository).save(any(MediaFile.class));
    }

    @Test
    @DisplayName("upload deve aceitar video com content-type de video")
    void shouldAcceptVideo() {
        MultipartFile video = FileStub.aVideo();
        when(minioService.upload(anyString(), any(MultipartFile.class))).thenReturn(FileStub.URL);
        when(minioService.getBucket()).thenReturn(FileStub.BUCKET);
        when(repository.save(any(MediaFile.class))).thenAnswer(inv -> inv.getArgument(0));

        FileResponse response = service.upload(video, FileType.VIDEO, FileStub.OWNER_ID);

        assertThat(response.type()).isEqualTo(FileType.VIDEO);
    }

    @Test
    @DisplayName("upload deve falhar com arquivo vazio")
    void shouldFailEmptyFile() {
        assertThatThrownBy(() -> service.upload(FileStub.anEmptyFile(), FileType.FOTO, FileStub.OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo vazio");

        verify(minioService, never()).upload(anyString(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("upload deve falhar quando content-type nao identificado")
    void shouldFailWithoutContentType() {
        assertThatThrownBy(() -> service.upload(FileStub.aFileWithoutContentType(), FileType.FOTO, FileStub.OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo do arquivo nao identificado");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("upload deve falhar quando FOTO recebe arquivo que nao e imagem")
    void shouldFailPhotoWithWrongType() {
        assertThatThrownBy(() -> service.upload(FileStub.aVideo(), FileType.FOTO, FileStub.OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo FOTO espera um arquivo de imagem");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("upload deve falhar quando VIDEO recebe arquivo que nao e video")
    void shouldFailVideoWithWrongType() {
        assertThatThrownBy(() -> service.upload(FileStub.aPhoto(), FileType.VIDEO, FileStub.OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo VIDEO espera um arquivo de video");

        verify(minioService, never()).upload(anyString(), any());
    }

    @Test
    @DisplayName("getById deve retornar arquivo existente")
    void shouldReturnById() {
        when(repository.findById(FileStub.ID)).thenReturn(Optional.of(FileStub.aFile().build()));

        FileResponse response = service.getById(FileStub.ID);

        assertThat(response.id()).isEqualTo(FileStub.ID);
    }

    @Test
    @DisplayName("getById deve falhar quando arquivo nao existe")
    void shouldFailGetByIdMissing() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo nao encontrado");
    }

    @Test
    @DisplayName("delete deve remover do MinIO e do banco quando e o dono")
    void shouldDelete() {
        MediaFile file = FileStub.aFile().build();
        when(repository.findById(FileStub.ID)).thenReturn(Optional.of(file));

        service.delete(FileStub.ID, FileStub.OWNER_ID);

        verify(minioService).delete(file.getStoredName());
        verify(repository).delete(file);
    }

    @Test
    @DisplayName("delete deve falhar quando nao e o dono")
    void shouldFailDeleteNotOwner() {
        MediaFile file = FileStub.aFile().build();
        when(repository.findById(FileStub.ID)).thenReturn(Optional.of(file));

        assertThatThrownBy(() -> service.delete(FileStub.ID, "outro-usuario"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao e o dono");

        verify(minioService, never()).delete(anyString());
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete deve falhar quando arquivo nao existe")
    void shouldFailDeleteMissing() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("inexistente", FileStub.OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo nao encontrado");

        verify(minioService, never()).delete(anyString());
    }
}
