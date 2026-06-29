package com.example.pos.dto;

import com.example.pos.api.DaySalesApi;
import com.example.pos.flow.DaySalesFlow;
import com.example.pos.flow.AuthFlow;
import com.example.pos.models.DaySalesReportData;
import com.example.pos.models.DaySalesView;
import com.example.pos.models.db.DaySalesReportPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DaySalesDto {

    @Autowired
    private DaySalesApi daySalesApi;

    @Autowired
    private AuthFlow authFlow;

    public List<DaySalesReportData> getDaySalesReport() {
        authFlow.checkSupervisor();

        List<DaySalesReportPojo> daySalesReportPojo = daySalesApi.getAllDaySales();
        return Helper.daySalesPojoListToDataList(daySalesReportPojo);
    }
}
