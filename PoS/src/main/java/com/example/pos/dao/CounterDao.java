package com.example.pos.dao;

import com.example.pos.models.db.CounterPojo;
import com.example.pos.models.db.CounterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CounterDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public CounterPojo incrementAndGetCounter(CounterType counterType) {

        Query query = new Query(Criteria.where("_id").is(counterType.name().toLowerCase()));
        Update update = new Update().inc("sequence", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        return mongoTemplate.findAndModify(query, update, options, CounterPojo.class);
    }
}