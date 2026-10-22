package com.hrbank.hrbank.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private String nextCursor;
    private Long nextIdAfter;
    private int size;
    private Long totalElements;
    private boolean hasNext;
}