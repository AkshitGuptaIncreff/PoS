package com.example.invoice;

import com.example.invoice.module.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    InvoiceDto invoiceDto;

    @RequestMapping(method = RequestMethod.POST)
    String invoiceGenerator(@RequestBody InvoiceForm invoiceForm){
        return invoiceDto.invoiceGenerator(invoiceForm);
    }

}
