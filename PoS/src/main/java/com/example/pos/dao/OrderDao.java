package com.example.pos.dao;

import com.example.pos.models.db.OrderPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderDao extends MongoRepository<OrderPojo, String> {

    List<OrderPojo> findByOrderTimeBetween(Instant startDate, Instant endDate);
}