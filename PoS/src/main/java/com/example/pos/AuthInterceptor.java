package com.example.pos;

import com.example.pos.flow.AuthFlow;
import com.example.pos.models.db.UserPojo;
import com.example.pos.util.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthFlow authFlow;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String sessionId = request.getHeader("sessionId");
        if(sessionId == null){
            throw new ApiException("Missing Session");
        }

        UserPojo user = authFlow.validateSession(sessionId);
        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}