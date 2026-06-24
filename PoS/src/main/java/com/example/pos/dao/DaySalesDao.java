package com.example.pos.dao;

import com.example.pos.models.db.DaySalesReportPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DaySalesDao extends MongoRepository<DaySalesReportPojo, String> {

    DaySalesReportPojo findByDate(LocalDate date);

    List<DaySalesReportPojo> findByDateBetween(LocalDate startDate, LocalDate endDate);

}
