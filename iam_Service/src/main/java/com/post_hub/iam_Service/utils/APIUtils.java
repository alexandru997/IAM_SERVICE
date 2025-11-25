package com.post_hub.iam_Service.utils;

import com.post_hub.iam_Service.model.constants.ApiConstants;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;

public class APIUtils {

    public static String getMethodName(){
        try{
            return Thread.currentThread().getStackTrace()[1].getMethodName();
        } catch (Exception e){
            return ApiConstants.UNDEFINED;
        }
    }

    public static Cookie createAuthCookie(String value) {
        Cookie authorizationCookie = new Cookie(HttpHeaders.AUTHORIZATION, value);
        authorizationCookie.setHttpOnly(true);
        authorizationCookie.setSecure(true);
        authorizationCookie.setPath("/");
        authorizationCookie.setMaxAge(300);
        return authorizationCookie;
    }
}
