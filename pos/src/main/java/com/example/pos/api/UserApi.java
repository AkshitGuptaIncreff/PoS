package com.example.pos.api;

import com.example.pos.dao.UserDao;
import com.example.pos.models.db.UserPojo;
import com.example.pos.models.db.UserRole;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserApi {

    @Autowired
    private UserDao userDao;

    public UserPojo findByEmail(String email){
        return userDao.findByEmail(email);
    }

    public UserPojo createNewUser(String email,String password,UserRole role){

        UserPojo userPojo = new UserPojo();
        userPojo.setEmail(email);
        userPojo.setCreatedAt(Instant.now());
        userPojo.setPasswordHash(password);
        userPojo.setRole(role);

        UserPojo user = userDao.save(userPojo);
        return user;
    }

    public UserPojo findById(String id){
        return userDao.findById(id).orElse(null);
    }
}
