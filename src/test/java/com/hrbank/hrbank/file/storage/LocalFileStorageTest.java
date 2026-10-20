package com.hrbank.hrbank.file;

import com.hrbank.hrbank.file.storage.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LocalFileStorageTest {


    @TempDir
    Path tempDir;

    @Test
    void storeAndLoadReturnsSameContent() throws IOException {

        LocalFileStorage storage =
                new LocalFileStorage(tempDir.toString());

        byte[] expected =
                "HR Bank test" .getBytes(StandardCharsets.UTF_8);

        UUID fileId = storage.store(
                new ByteArrayInputStream(expected),
                "test.txt"
        );

        try (InputStream inputStream = storage.load(fileId)) {
            byte[] actual = inputStream.readAllBytes();

            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void deleteRemovesFile() throws IOException {

        // given
        LocalFileStorage storage =
                new LocalFileStorage(tempDir.toString());

        UUID fileId = storage.store(
                new ByteArrayInputStream(
                        "test".getBytes(StandardCharsets.UTF_8)
                ), "test.txt"
        );

        storage.delete(fileId);
        assertThrows(IOException.class,
                () -> storage.load(fileId));
    }

    @Test
    void loadMissingFileThrowsIOException() {
        // given

        LocalFileStorage storage =
                new LocalFileStorage(tempDir.toString());

        UUID fileId = UUID.randomUUID();

        assertThrows(
                IOException.class, () -> storage.load(fileId)
        );
    }

    @Test
    void deleteMissingFileThrowsIOException() {
        // given

        LocalFileStorage storage = new LocalFileStorage(tempDir.toString());

        UUID fileId = UUID.randomUUID();

        assertThrows(IOException.class, () -> storage.delete(fileId));
    }

    // @TempDir : 테스트 전용 임시 디렉터리를 제공
    // UUID.randomUUID() : 테스트용 파일 ID 생성
    // assertArrayEquals() : 실제 파일 내용과 원본 내용 비교
    // assertThrows() : 예상한 예외가 발생하는지 확인
    // try-with-resources : 파일을 읽은 뒤 InputStream을 자동으로 닫음

    // Given : 테스트에 필요한 객체나 데이털르 준비해라. -> 파일 메타 데이터 객체를 만들고 테스트 환경을 설정한다.
    // When : 실제로 테스트할 메서드를 호출해라 -> 파일 메타데이터를 저장하는 Service 메서드를 실행한다.
    // Then : 실행 결과를 예상대인지 확인해라 -> 저장 메서드가 호출 됐는지, 반환값이 올바른지 검증한다.

}
