package com.example.pos.flow;

import com.example.pos.AuthProperties;
import com.example.pos.UserContext;
import com.example.pos.api.UserApi;
import com.example.pos.api.SessionApi;
import com.example.pos.models.AuthData;
import com.example.pos.models.AuthForm;
import com.example.pos.models.db.SessionPojo;
import com.example.pos.models.db.UserPojo;
import com.example.pos.models.db.UserRole;
import com.example.pos.util.ApiException;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class AuthFlow {
    @Autowired
    private UserApi userApi;

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SessionApi sessionApi;


    public AuthData login(UserPojo userPojo) {
        UserPojo user = userApi.findByEmail(userPojo.getEmail());

        if (user == null) {
            throw new ApiException("Invalid Cred");
        }

        if (!passwordEncoder.matches(userPojo.getPasswordHash(), user.getPasswordHash())) {
            throw new ApiException("Invalid Cred");
        }

        SessionPojo session = sessionApi.createSession(user);
        AuthData data = new AuthData();
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        data.setSessionId(session.getSessionId());
        return data;
    }

    public AuthData signUp(AuthForm form){

        String email = Helper.trimAndLowercase(form.getEmail());
        UserPojo user = userApi.findByEmail(email);

        if(user != null){
            throw new ApiException("User Exists");
        }

        String passwordHash = passwordEncoder.encode(form.getPassword());

        UserPojo userPojo = userApi.createNewUser(email,passwordHash,getRole(email));

        SessionPojo session = sessionApi.createSession(userPojo);
        AuthData data = new AuthData();

        data.setEmail(userPojo.getEmail());
        data.setRole(userPojo.getRole());
        data.setSessionId(session.getSessionId());

        return data;
    }

    private UserRole getRole(String email){

        List<String> supervisors = new ArrayList<>();
        for(String supervisor : authProperties.getSupervisors().split(",") ){
            supervisors.add(supervisor.trim().toLowerCase());
        }

        return supervisors.contains(email) ? UserRole.SUPERVISOR : UserRole.OPERATOR;
    }

    public UserPojo validateSession(String sessionId, HttpServletResponse response) {
        SessionPojo session = sessionApi.findBySessionId(sessionId);
        if (session == null) {
            throw new ApiException("Invalid session");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessionApi.delete(session.getId());
            throw new ApiException("Session expired");
        }

        Instant now = Instant.now();
        String finalSessionId = session.getSessionId();

        if (now.isAfter(session.getCreatedAt().plus(5, ChronoUnit.MINUTES))) {

            // Rotate session ID in-place
            String newSessionId = UUID.randomUUID().toString();
            session.setSessionId(newSessionId);
            session.setCreatedAt(now);
            session.setExpiresAt(now.plus(5, ChronoUnit.MINUTES));
            sessionApi.updateSession(session);
            finalSessionId = newSessionId;

            // Set response header and cookie so the client picks up the new ID
            response.setHeader("sessionId", newSessionId);
            Cookie cookie = new Cookie("sessionId", newSessionId);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(300);
            cookie.setSecure(false);
            try {
                cookie.setAttribute("SameSite", "Lax");
            } catch (NoSuchMethodError ignored) {}
            response.addCookie(cookie);
        } else {
            session.setExpiresAt(now.plus(5, ChronoUnit.MINUTES));
            sessionApi.updateSession(session);
        }

        UserPojo user = userApi.findById(session.getUserId());
        if (user == null) {
            throw new ApiException("User not found");
        }

        UserContext.setSessionId(finalSessionId);
        return user;
    }

    public void checkSupervisor(){

        UserPojo user = UserContext.getUser();

        if(user == null){
            throw new ApiException("Unauthorized");
        }

        if(user.getRole() != UserRole.SUPERVISOR){
            throw new ApiException("Forbidden");
        }
    }
}