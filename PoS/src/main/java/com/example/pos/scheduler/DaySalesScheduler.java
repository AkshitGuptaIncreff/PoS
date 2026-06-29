package com.example.pos.scheduler;

import com.example.pos.flow.DaySalesFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class DaySalesScheduler {
    @Autowired
    private DaySalesFlow daySalesFlow;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void generateDaySales() {
        System.out.println("=== Scheduler Started ===");
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        System.out.println("Generating report for " + yesterday);
        daySalesFlow.generateDaySales(yesterday);
        System.out.println("=== Scheduler Finished ===");
    }
}
