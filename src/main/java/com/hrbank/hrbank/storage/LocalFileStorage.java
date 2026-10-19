package com.hrbank.hrbank.storage;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Component
public class LocalFileStorage implements FileStorage {

        private final Path storagePath;

    // application.yml에서 저장 위치를 설정할 수 있도록 한다.
        public LocalFileStorage(
                @Value("${file.storage.location:uploads}") String location) {
            this.storagePath = Paths.get(location).toAbsolutePath().normalize();
        }



    @Override
    public UUID store(InputStream inputStream, String fileName) throws IOException {

                UUID fileId = UUID.randomUUID();

                Path filePath = storagePath.resolve(fileId.toString());

                Files.createDirectories(storagePath);

                Files.copy(inputStream, filePath);

                return fileId;

    }

    @Override
    public InputStream load(UUID fileId) throws IOException {

        Path filePath = storagePath.resolve(fileId.toString());

        if (!Files.isRegularFile(filePath)) {
            throw new IOException("파일을 찾을 수 없습니다: " + fileId);

        }

        return Files.newInputStream(filePath);
    }

    @Override
    public void delete(UUID fileId) throws IOException {

            Path filePath = storagePath.resolve(fileId.toString());

            if (!Files.deleteIfExists(filePath)) {
                throw new IOException("삭제할 파일이 없습니다: " + fileId);

            }

    }
}
