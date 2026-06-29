package com.example.pos;

import com.example.pos.flow.DaySalesFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/test")
public class TestScheduler {
    @Autowired
    private DaySalesFlow daySalesFlow;

    @PostMapping("/generate-day-sales")
    public String generate(){
        daySalesFlow.generateDaySales(LocalDate.of(2026, 6, 19));
        return "done";
    }
}