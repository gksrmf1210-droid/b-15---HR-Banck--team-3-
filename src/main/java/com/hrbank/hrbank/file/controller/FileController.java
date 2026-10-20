    package com.hrbank.hrbank.file.controller;


    import com.hrbank.hrbank.entity.FileMeta;
    import com.hrbank.hrbank.service.FileService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.core.io.InputStreamResource;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;

    import java.io.IOException;
    import java.io.InputStream;
    import java.util.UUID;

    @Tag(name = "파일 관리", description = "파일 업로드, 다운로드 및 삭제 API")
    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/files")
    public class FileController {

        private final FileService fileService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "파일 업로드", description = "파일을 업로드하고 파일 메타데이터를 반환한다. ")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "파일 업로드 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 파일 요청")
        })


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
        @Operation(summary = "파일 다운로드", description = "파일 ID를 이용해 파일을 다운로드 한다.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
                @ApiResponse(responseCode = "404", description = "파일 또는 메타데이터를 찾을 수 없음")
        })



        public ResponseEntity<InputStreamResource> download(
                @Parameter(description = "다운로드할 파일의 UUID", required = true)
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
        @Operation(summary = "파일 삭제", description = "파일과 해당 메타데이터를 삭제한다.")
        @ApiResponses({
                @ApiResponse(responseCode = "204", description = "파일 삭제 성공"),
                @ApiResponse(responseCode = "404", description = "파일 또는 메타데이터를 찾을 수 없음.")
        })

        public ResponseEntity<Void> delete(
                @Parameter(description = "삭제할 파일의 UUID", required = true)
                @PathVariable UUID fileId
                ) throws IOException {

            fileService.delete(fileId);

            return ResponseEntity.noContent().build();
        }


    }
