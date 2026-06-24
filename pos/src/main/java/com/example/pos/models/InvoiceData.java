package com.example.pos.models;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class InvoiceData {
    private ResponseEntity<Resource>
    buildResponse(byte[] pdf){

        ByteArrayResource resource =
                new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice.pdf")
                .contentLength(pdf.length)
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
