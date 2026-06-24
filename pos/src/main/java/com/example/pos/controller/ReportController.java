package com.example.pos.controller;

import com.example.pos.dto.ReportDto;
import com.example.pos.models.DaySalesReportForm;
import com.example.pos.models.SalesReportData;
import com.example.pos.models.SalesReportForm;
import com.example.pos.models.db.DaySalesReportPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportDto reportDto;

    @RequestMapping(method = RequestMethod.GET,path = "/sales")
    public List<SalesReportData> getSalesReport(@RequestBody SalesReportForm form) {
        return reportDto.getSalesReport(form);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/day-sales")
    public List<DaySalesReportPojo> getDaySalesReport(@RequestBody DaySalesReportForm form){
        return reportDto.getDaySalesReport(form);
    }
}
