package com.trisha.midia.controller;

import com.trisha.midia.auth.AuthenticatedUser;
import com.trisha.midia.model.dto.response.FileResponse;
import com.trisha.midia.model.enums.FileType;
import com.trisha.midia.service.FileService;
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
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public FileResponse upload(AuthenticatedUser user,
                               @RequestParam("arquivo") MultipartFile file,
                               @RequestParam("tipo") FileType type) {
        return fileService.upload(file, type, user.id());
    }

    @GetMapping("/{id}")
    public FileResponse getById(@PathVariable String id) {
        return fileService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(AuthenticatedUser user, @PathVariable String id) {
        fileService.delete(id, user.id());
    }
}
