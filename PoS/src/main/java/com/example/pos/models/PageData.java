package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PageData<T> {

    private List<T> content;

    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
