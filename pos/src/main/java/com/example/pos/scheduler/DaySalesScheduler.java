package com.example.pos.scheduler;

import com.example.pos.api.DaySalesApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class DaySalesScheduler {
    @Autowired
    private DaySalesApi daySalesApi;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void generateDaySales() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        daySalesApi.generateDaySales(yesterday);
    }
}
