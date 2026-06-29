package com.example.pos;

import com.example.pos.flow.DaySalesFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/debug")
public class DebugController {

    public DebugController(){
        System.out.println("Day Sales Test Loaded");
    }

    @Autowired
    private DaySalesFlow daySalesFlow;

    @RequestMapping(method = RequestMethod.GET, path = "/day-sales")
    public String runDaySales() {
        daySalesFlow.generateDaySales(LocalDate.of(2026, 6, 28));
        return "Triggered";
    }
}
