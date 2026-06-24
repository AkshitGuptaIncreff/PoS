package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class DaySalesReportForm {

    private LocalDate startDate;

    private LocalDate endDate;
}
