package com.example.pos.api;

import com.example.pos.dao.InvoiceDao;
import lombok.RequiredArgsConstructor;
import com.example.pos.models.db.InvoicePojo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceApi {

    private final InvoiceDao invoiceDao;

    public InvoicePojo findByOrderId(String orderId) {
        return invoiceDao.findByOrderId(orderId);
    }

    public InvoicePojo save(InvoicePojo invoicePojo) {
        return invoiceDao.save(invoicePojo);
    }
}