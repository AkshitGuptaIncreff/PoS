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
import com.example.pos.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


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


    public AuthData login(AuthForm form){

        String email = Utils.trimAndLowercase(form.getEmail());
        UserPojo user = userApi.findByEmail(email);

        if(user == null){
            throw new ApiException("Invalid Cred");
        }

        if(!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())){
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

        String email = Utils.trimAndLowercase(form.getEmail());
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

    public UserPojo validateSession(String sessionId){

        SessionPojo session = sessionApi.findBySessionId(sessionId);
        if(session == null){
            throw new ApiException("Invalid session");
        }
        if(session.getExpiresAt().isBefore(Instant.now())){
            throw new ApiException("Session expired");
        }

        UserPojo user = userApi.findById(session.getUserId());
        if(user == null){
            throw new ApiException("User not found");
        }
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