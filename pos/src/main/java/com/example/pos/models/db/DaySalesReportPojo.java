package com.example.pos.models.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "pos_day_sales")
@Getter
@Setter
public class DaySalesReportPojo {
    @Id
    private String id;

    @Indexed(unique = true)
    private LocalDate date;

    private Integer invoicedOrdersCount;

    private Integer invoicedItemsCount;

    private Double totalRevenue;
}
