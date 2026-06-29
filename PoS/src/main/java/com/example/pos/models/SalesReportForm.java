package com.example.pos.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class SalesReportForm {

    @NotNull(message = "Start date is required")
    private ZonedDateTime startDate;

    @NotNull(message = "End date is required")
    private ZonedDateTime endDate;

    @NotNull(message = "Client Id not found")
    private String clientId;
}