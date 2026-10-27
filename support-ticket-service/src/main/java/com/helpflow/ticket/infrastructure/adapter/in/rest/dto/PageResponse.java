package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <D, T> PageResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        return new PageResponse<>(result.content().stream().map(mapper).toList(), result.page(), result.size(),
                result.totalElements(), result.totalPages());
    }
}
