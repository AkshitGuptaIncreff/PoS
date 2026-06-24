package com.example.pos.models.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
public class InvoicePojo {
    @Id
    private String id;
    private String orderId;
    private String invoiceNumber;
    private String pdfPath;
    private Instant generatedAt;
}
