package com.example.pos.dao;

import com.example.pos.models.db.UserPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends MongoRepository<UserPojo, String> {

    UserPojo findByEmail(String email);
}