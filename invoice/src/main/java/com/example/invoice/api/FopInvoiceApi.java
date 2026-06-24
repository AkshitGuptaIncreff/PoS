package com.example.invoice.api;

import com.example.invoice.utilis.invoice.PdfGeneratorUtil;
import com.example.invoice.utilis.invoice.XmlUtil;
import com.example.invoice.module.InvoiceForm;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class FopInvoiceApi implements invoiceApi{

    @Override
    public String invoiceGenerator(InvoiceForm invoiceForm) {

        try {
            String xml = XmlUtil.toXml(invoiceForm);
            byte[] pdf = PdfGeneratorUtil.generatePdf(xml);

            Files.write(Path.of("invoice.pdf"), pdf);
            System.out.println("PDF generated successfully");
            return Base64.getEncoder().encodeToString(pdf);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

}
