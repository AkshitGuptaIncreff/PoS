package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SalesReportForm {

    private Instant startDate;

    private Instant endDate;

    private String clientName;
}