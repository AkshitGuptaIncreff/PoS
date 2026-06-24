package com.example.invoice.utilis.invoice;

import org.apache.fop.apps.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import java.io.*;

public class PdfGeneratorUtil {

    public static byte[] generatePdf(String xml) {

        // (1) FopFactory - Creates FOP engines.
        // (2) FOUserAgent - PDF rendering configuration.
        // (3) Fop - Actual PDF generator.
        // (4) Transformer - Applies XSL rules to XML.

        try {
            // create output buffer, FopFactory loaded, Fop instance & metadata given to pdf
            ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            // Output type PDF, use these settings and write in PDF
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOutput);

            // built-in XSLT engine (xml + xsl = another xml normally)
            TransformerFactory factory = TransformerFactory.newInstance();
            InputStream xslStream = PdfGeneratorUtil.class.getClassLoader().getResourceAsStream("invoice.xsl");

            // template loaded ready to apply, input data,
            Transformer transformer = factory.newTransformer(new StreamSource(xslStream));
            Source xmlSource = new StreamSource(new StringReader(xml));

            // FO events directly to Apache FOP (less intermediate files)
            Result result = new SAXResult(fop.getDefaultHandler());

            // everything applied here
            transformer.transform(xmlSource, result);
            return pdfOutput.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
