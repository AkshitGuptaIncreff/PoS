package com.example.pos.dao;

import com.example.pos.models.db.SessionPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionDao extends MongoRepository<SessionPojo, String> {
    SessionPojo findBySessionId(String sessionId);
}
