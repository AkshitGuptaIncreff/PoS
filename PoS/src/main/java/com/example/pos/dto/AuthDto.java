package com.example.pos.dto;

import com.example.pos.UserContext;
import com.example.pos.flow.AuthFlow;
import com.example.pos.models.AuthData;
import com.example.pos.models.AuthForm;
import com.example.pos.models.db.UserPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthDto {

    @Autowired
    private AuthFlow authFlow;

    public AuthData login(AuthForm form) {
        String email = Helper.trimAndLowercase(form.getEmail());
        form.setEmail(email);

        UserPojo userPojo = Helper.authFormToUserPojo(form);
        return authFlow.login(userPojo);
    }

    public AuthData signup(AuthForm form){
        return authFlow.signUp(form);
    }

    public AuthData validate(UserPojo user){
        return Helper.authPojoToData(user, UserContext.getSessionId());
    }
}
