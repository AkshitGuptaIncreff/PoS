package com.example.pos.controller;

import com.example.pos.UserContext;
import com.example.pos.dto.AuthDto;
import com.example.pos.models.AuthData;
import com.example.pos.models.AuthForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthDto authDto;

    @RequestMapping(method = RequestMethod.POST, path = "/signup")
    public AuthData signup(@RequestBody AuthForm form){
        return authDto.signup(form);
    }

    @RequestMapping(method = RequestMethod.POST, path ="/login")
    public AuthData login(@RequestBody AuthForm form){
        return authDto.login(form);
    }

    @RequestMapping(method = RequestMethod.POST, path ="/validate")
    public AuthData validate(){
        return authDto.validate(UserContext.getUser());
    }
}