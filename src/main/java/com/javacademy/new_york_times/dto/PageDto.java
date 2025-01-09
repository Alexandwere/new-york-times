package com.javacademy.new_york_times.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDto <T> {
    private List<T> content;
    private Integer countPages;
    private Integer currentPage;
    private Integer maxPageSize;
    private Integer size;
}
