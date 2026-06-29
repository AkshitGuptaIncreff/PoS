package com.example.pos.controller;

import com.example.pos.dto.InvoiceDto;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
@RestController("posInvoiceController")
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    InvoiceDto invoiceDto;

    @RequestMapping(method = RequestMethod.GET, path = "/{orderId}")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable String orderId) {
        return invoiceDto.downloadInvoice(orderId);
    }
}
