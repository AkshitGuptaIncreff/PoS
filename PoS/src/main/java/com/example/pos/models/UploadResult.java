package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UploadResult<T> {
    private List<T> imported;
    private List<RowError> errors;
    private int totalRows;
    private int importedCount;
    private int errorCount;
}
