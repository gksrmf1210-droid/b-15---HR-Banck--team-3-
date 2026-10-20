package com.hrbank.hrbank.file.service;

import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.file.storage.FileStorage;
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
            String contentType,
            long size

    ) throws IOException {
        UUID storageKey = fileStorage.store(inputStream, fileName);

        try {
            FileMeta fileMeta = new FileMeta(
                    storageKey,
                    fileName,
                    contentType,
                    size

            );


            return fileMetaService.save(fileMeta);
        } catch (RuntimeException e) {
            try {
                fileStorage.delete(storageKey);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw  e;
        }
    }
    public InputStream download (Long fileId) throws IOException {

       FileMeta fileMeta= findMetadata(fileId);
       return fileStorage.load(fileMeta.getStorageKey());


    }


    public void delete(Long fileId) throws IOException {

       FileMeta fileMeta = findMetadata(fileId);

       fileStorage.delete(fileMeta.getStorageKey());
       fileMetaService.deleteById(fileId);



}

public FileMeta findMetadata(Long fileId) {
       return fileMetaService.findById(fileId).orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다." + fileId));

}

}
