package com.hrbank.hrbank.file.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface FileStorage {

    UUID store(InputStream inputStream, String fileName) throws IOException;

    InputStream load(UUID fileId) throws IOException;

    void delete(UUID fileId) throws IOException;
}
