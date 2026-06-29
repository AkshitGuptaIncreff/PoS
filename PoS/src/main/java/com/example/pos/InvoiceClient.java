package com.example.pos;

import com.example.invoice.module.InvoiceForm;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("posInvoiceClient")
public class InvoiceClient {

    private final RestClient restClient;

    public InvoiceClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8040").build();
    }

    public String generateInvoice(InvoiceForm form) {
        String sessionId = UserContext.getSessionId();
        var request = restClient.post().uri("/invoice").body(form);
        if (sessionId != null) {
            request = request.header("sessionId", sessionId);
        }
        return request.retrieve().body(String.class);
    }
}