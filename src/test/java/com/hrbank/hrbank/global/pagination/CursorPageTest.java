package com.hrbank.hrbank.global.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CursorPageTest {


    @Test
    @DisplayName("다음 페이지가 있으면 커서와 여부를 반환한다.")
    void returnsCursorAndHasNextWhenNextPageExists() {
        List<String> content = List.of("직원1", "직원2");

        CursorPage<String> page =
                CursorPage.of(content, "102", true);

        assertEquals(content, page.content());
        assertEquals("102", page.nextCursor());
        assertTrue(page.hasNext());
    }

    @Test
    @DisplayName("마지막 페이지는 다음 커서가 없을 수 있다.")
    void allowsNullCursorWhenLastPage() {
        List<String> content = List.of("직원3");

        CursorPage<String> page =
                CursorPage.of(content, null, false);

        assertEquals(content, page.content());
        assertNull(page.nextCursor());
        assertFalse(page.hasNext());
    }


    @Test
    @DisplayName("빈 목록도 생성할 수 있다.")
    void createsPageWithEmptyContent() {
        CursorPage<String> page =
                CursorPage.of(List.of(), null, false);

        assertTrue(page.content().isEmpty());
        assertNull(page.nextCursor());
        assertFalse(page.hasNext());


    }

    @Test
    @DisplayName("생성된 페이지의 목록은 수정할 수 없다.")
    void preventsModificationOfPageContent() {
        CursorPage<String> page =
                CursorPage.of(List.of("직원1"), "1", true);

        assertThrows(
                UnsupportedOperationException.class, () ->page.content().add("직원2")
        );
    }
}


