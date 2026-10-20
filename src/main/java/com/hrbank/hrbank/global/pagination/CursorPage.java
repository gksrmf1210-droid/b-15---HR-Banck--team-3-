package com.hrbank.hrbank.global.pagination;

import java.util.List;

public record CursorPage<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {

    public CursorPage {
        content = List.copyOf(content);

    }

    public static <T> CursorPage<T> of(
            List<T> content,
            String nextCursor,
            boolean hasNext
    ) {
        return new CursorPage<>(content, nextCursor, hasNext);
    }






}
