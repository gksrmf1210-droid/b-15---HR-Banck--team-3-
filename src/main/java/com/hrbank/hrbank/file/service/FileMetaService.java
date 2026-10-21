package com.hrbank.hrbank.file.service;


import com.hrbank.hrbank.file.entity.FileMeta;
import com.hrbank.hrbank.file.repository.FileMetaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class FileMetaService {

    private final FileMetaRepository fileMetaRepository;

    public FileMetaService(FileMetaRepository fileMetaRepository) {
        this.fileMetaRepository = fileMetaRepository;
    }

    public FileMeta save(FileMeta fileMeta) {
        return fileMetaRepository.save(fileMeta);
    }

    public Optional<FileMeta> findById(Long fileId) {
        return fileMetaRepository.findById(fileId);
    }

    public void deleteById(Long fileId) {
        fileMetaRepository.deleteById(fileId);
    }

}







