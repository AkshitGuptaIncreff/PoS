package com.example.pos.api;

import com.example.pos.dao.SessionDao;
import com.example.pos.models.db.SessionPojo;
import com.example.pos.models.db.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionApi {

    @Autowired
    SessionDao sessionDao;

    public SessionPojo createSession(UserPojo user){

        SessionPojo session = new SessionPojo();

        session.setUserId(user.getId());
        session.setSessionId(UUID.randomUUID().toString());
        session.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));

        return sessionDao.save(session);
    }

    public SessionPojo findBySessionId(String sessionId){
        return sessionDao.findBySessionId(sessionId);
    }

    public void delete(String sessionId){
        sessionDao.deleteById(sessionId);
    }
}