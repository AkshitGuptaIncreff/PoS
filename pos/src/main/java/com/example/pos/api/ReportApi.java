package com.example.pos.api;

import com.example.pos.dao.ReportsDao;
import com.example.pos.models.SalesReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReportApi {
    @Autowired
    ReportsDao reportsDao;


    public List<SalesReportData> getSalesReport(Instant start,Instant end,String name){
        return reportsDao.getSalesReport(start,end,name);
    }

}
