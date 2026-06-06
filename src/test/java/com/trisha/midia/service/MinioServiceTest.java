package com.trisha.midia.service;

import com.trisha.midia.stub.ArquivoStub;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinioService")
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "bucket", ArquivoStub.BUCKET);
    }

    @Test
    @DisplayName("getBucket deve retornar o bucket configurado")
    void deveRetornarBucket() {
        assertThat(service.getBucket()).isEqualTo(ArquivoStub.BUCKET);
    }

    @Test
    @DisplayName("upload deve enviar objeto e devolver a presigned URL")
    void deveFazerUpload() throws Exception {
        MultipartFile foto = ArquivoStub.umaFoto();
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(ArquivoStub.URL);

        String url = service.upload("uuid-gerado.jpg", foto);

        assertThat(url).isEqualTo(ArquivoStub.URL);
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("upload deve encapsular falha do MinIO em RuntimeException")
    void deveFalharUpload() throws Exception {
        MultipartFile foto = ArquivoStub.umaFoto();
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("conexao recusada"));

        assertThatThrownBy(() -> service.upload("uuid-gerado.jpg", foto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao enviar arquivo ao MinIO");
    }

    @Test
    @DisplayName("delete deve remover o objeto do bucket")
    void deveDeletar() throws Exception {
        service.delete("uuid-gerado.jpg");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("delete deve encapsular falha do MinIO em RuntimeException")
    void deveFalharDelete() throws Exception {
        doThrow(new RuntimeException("objeto inexistente"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> service.delete("uuid-gerado.jpg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao remover arquivo do MinIO");
    }
}
