package com.example.pos;

import com.example.pos.api.DaySalesApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/test")
public class TestScheduler {
    @Autowired
    private DaySalesApi daySalesApi;

    @PostMapping("/generate-day-sales")
    public String generate(){
        daySalesApi.generateDaySales(LocalDate.of(2026, 6, 19));
        return "done";
    }
}
