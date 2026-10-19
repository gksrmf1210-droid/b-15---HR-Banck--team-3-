package com.hrbank.hrbank.service;


import com.hrbank.hrbank.entity.FileMeta;
import com.hrbank.hrbank.repository.FileMetaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FileMetaService {

    private final FileMetaRepository fileMetaRepository;

    public FileMetaService(FileMetaRepository fileMetaRepository) {
        this.fileMetaRepository = fileMetaRepository;


    }

    public FileMeta save(FileMeta fileMeta) {
        return  fileMetaRepository.save(fileMeta);

    }

    public Optional<FileMeta> findById(UUID fileId) {
        return fileMetaRepository.findById(fileId);
    }

    public void deleteById(UUID fileId) {
        fileMetaRepository.deleteById(fileId);
    }
    


}
