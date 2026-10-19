package com.hrbank.hrbank.service;

import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileService {

    private final FileStorage fileStorage;
    private final FileMetaService fileMetaService;

   public FileService(
           FileStorage fileStorage,
           FileMetaService fileMetaService
   ) {
       this.fileStorage = fileStorage;
       this.fileMetaService = fileMetaService;
    }

    public FileMeta upload(
            InputStream inputStream,
            String fileName,
            String cotentType,
            long size

    ) throws IOException {
        UUID fileId = fileStorage.store(inputStream, fileName);

        try {
            FileMeta fileMeta = new FileMeta(
                    fileId,
                    fileName,
                    cotentType,
                    size

            );
            return fileMetaService.save(fileMeta);
        } catch (RuntimeException e) {
            try {
                fileStorage.delete(fileId);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw  e;
        }
    }
    public InputStream download (UUID fileId) throws IOException {
       fileMetaService.findById(fileId).orElseThrow(() -> new IllegalArgumentException(
               "파일 정보를 찾을 수 없습니다." + fileId
               )
       );
       return fileStorage.load(fileId);
    }


    public void delete(UUID fileId) throws IOException {

       fileMetaService.findById(fileId).orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다" + fileId)
       );

       fileStorage.delete(fileId);

       fileMetaService.deleteById(fileId);
}

public FileMeta findMetadata(UUID fileId) {
       return fileMetaService.findById(fileId).orElseThrow(() -> new IllegalArgumentException("파일 정볼르 찾을 수 없습니다." + fileId));

}

}
