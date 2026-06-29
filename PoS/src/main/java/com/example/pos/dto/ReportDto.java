package com.example.pos.dto;

import com.example.pos.api.ReportApi;
import com.example.pos.flow.AuthFlow;
import com.example.pos.util.Helper;
import com.example.pos.models.SalesReportData;
import com.example.pos.models.SalesReportForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.List;

@Component
public class ReportDto {

    @Autowired
    private ReportApi reportApi;

    @Autowired
    AuthFlow authFlow;

    public List<SalesReportData> getSalesReport(SalesReportForm form) {
        authFlow.checkSupervisor();

        String client = form.getClientId();
        Helper.validateSalesReport(form);

        return reportApi.getSalesReport(form.getStartDate().toInstant(),form.getEndDate().toInstant(),client);
    }
}