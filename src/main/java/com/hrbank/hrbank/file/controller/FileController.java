package com.hrbank.hrbank.file.controller;


import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMeta> upload(
            @RequestParam("file") MultipartFile file
            ) throws IOException {

        FileMeta fileMeta = fileService.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        return ResponseEntity.ok(fileMeta);
    }


    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID fileId
            ) throws IOException {

        fileService.delete(fileId);

        return ResponseEntity.noContent().build();
    }


}
