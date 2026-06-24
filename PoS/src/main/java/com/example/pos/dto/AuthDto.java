package com.example.pos.dto;

import com.example.pos.flow.AuthFlow;
import com.example.pos.models.AuthData;
import com.example.pos.models.AuthForm;
import com.example.pos.models.db.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthDto {

    @Autowired
    private AuthFlow authFlow;

    public AuthData login(AuthForm form){
        return authFlow.login(form);
    }

    public AuthData signup(AuthForm form){
        return authFlow.signUp(form);
    }

    public AuthData validate(String sessionId){

        UserPojo user = authFlow.validateSession(sessionId);

        AuthData data = new AuthData();
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        data.setSessionId(sessionId);
        return data;
    }
}
