package com.example.invoice;

import com.example.invoice.utilis.invoice.XmlUtil;
import com.example.invoice.module.InvoiceForm;
import com.example.invoice.module.InvoiceItems;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

public class XmlConversionTest {

    @Test
    void shouldGenerateXml() {

        InvoiceItems item1 = new InvoiceItems();
        item1.setBarcode("123");
        item1.setProductName("Milk");
        item1.setQuantity(2);
        item1.setSellingPrice(50.0);

        InvoiceItems item2 = new InvoiceItems();
        item2.setBarcode("456");
        item2.setProductName("Bread");
        item2.setQuantity(1);
        item2.setSellingPrice(30.0);

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-001");
        form.setOrderId("ORD-001");
        form.setCustomerName("Akshit");
        form.setOrderTime(Instant.now());
        form.setTotalAmount(130.0);
        form.setItems(List.of(item1, item2));
        System.out.println(XmlUtil.toXml(form));
    }
}