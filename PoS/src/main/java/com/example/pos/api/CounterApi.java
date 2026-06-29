package com.example.pos.api;

import com.example.pos.dao.CounterDao;
import com.example.pos.models.db.CounterPojo;
import com.example.pos.models.db.CounterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterApi {

    @Autowired
    private CounterDao counterDao;

    public long getNextSequence(CounterType counterType) {
        CounterPojo counter = counterDao.incrementAndGetCounter(counterType);
        return counter.getSequence();
    }
}
