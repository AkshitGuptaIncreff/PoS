package com.example.pos.dto;

import com.example.pos.api.DaySalesApi;
import com.example.pos.api.ReportApi;
import com.example.pos.flow.AuthFlow;
import com.example.pos.util.Utils;
import com.example.pos.models.DaySalesReportForm;
import com.example.pos.models.SalesReportData;
import com.example.pos.models.SalesReportForm;
import com.example.pos.models.db.DaySalesReportPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportDto {

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private DaySalesApi daySalesApi;

    @Autowired
    AuthFlow authFlow;

    public List<SalesReportData> getSalesReport(SalesReportForm form) {
        authFlow.checkSupervisor();

        String client = form.getClientName();
        client = Utils.trimAndLowercase(client);

        return reportApi.getSalesReport(form.getStartDate(),form.getEndDate(),client);
    }

    public List<DaySalesReportPojo> getDaySalesReport(DaySalesReportForm form){
        authFlow.checkSupervisor();

        return daySalesApi.getDaySalesReport(form.getStartDate(), form.getEndDate());
    }

}