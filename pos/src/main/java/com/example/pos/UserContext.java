package com.example.pos;

import com.example.pos.models.db.UserPojo;

public class UserContext {

    private static final ThreadLocal<UserPojo> user = new ThreadLocal<>();

    public static void setUser(UserPojo userPojo){
        user.set(userPojo);
    }

    public static UserPojo getUser(){
        return user.get();
    }

    public static void clear(){
        user.remove();
    }
}
