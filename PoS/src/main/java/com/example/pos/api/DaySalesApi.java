package com.example.pos.api;

import com.example.pos.dao.DaySalesDao;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.db.DaySalesReportPojo;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.OrderPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
public class DaySalesApi {

    @Autowired
    private DaySalesDao daySalesDao;

    public List<DaySalesReportPojo> getAllDaySales() {
        return daySalesDao.findAllByOrderByDateDesc();
    }

    public DaySalesReportPojo findByDate(LocalDate date) {
        return daySalesDao.findByDate(date);
    }

    public void save(DaySalesReportPojo daySales) {
        daySalesDao.save(daySales);
    }
}