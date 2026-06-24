package com.example.pos.dao;

import com.example.pos.models.db.InvoicePojo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceDao extends MongoRepository<InvoicePojo, String> {

    InvoicePojo findByOrderId(String orderId);
}