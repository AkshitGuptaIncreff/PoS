package com.example.pos.controller;

import com.example.pos.dto.DaySalesDto;
import com.example.pos.dto.ReportDto;
import com.example.pos.models.DaySalesReportData;
import com.example.pos.models.SalesReportData;
import com.example.pos.models.SalesReportForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
@RestController
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private ReportDto reportDto;

    @Autowired
    private DaySalesDto daySalesDto;

    @RequestMapping(method = RequestMethod.POST,path = "/sales")
    public List<SalesReportData> getSalesReport(@Valid @RequestBody SalesReportForm form) {
        return reportDto.getSalesReport(form);
    }

    @RequestMapping(method = RequestMethod.GET,path = "/day-sales")
    public List<DaySalesReportData> getDaySalesReport(){
        return daySalesDto.getDaySalesReport();
    }
}
