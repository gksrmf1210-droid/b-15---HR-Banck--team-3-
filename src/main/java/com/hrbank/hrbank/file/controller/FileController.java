package com.hrbank.hrbank.file.controller;


import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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

    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable UUID fileId
            ) throws IOException  {



        FileMeta fileMeta = fileService.findMetadata(fileId);
        InputStream inputStream = fileService.download(fileId);

        org.springframework.core.io.InputStreamResource resource =
                new org.springframework.core.io.InputStreamResource(inputStream);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(fileMeta.getContentType()))
                .header(
                        org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(fileMeta.getFileName(), java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentLength(fileMeta.getSize())
                .body(resource);


    }


    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID fileId
            ) throws IOException {

        fileService.delete(fileId);

        return ResponseEntity.noContent().build();
    }


}
