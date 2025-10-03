package com.post_hub.iam_Service.utils;

import com.post_hub.iam_Service.model.constants.ApiConstants;

public class APIUtils {

    public static String getMethodName(){
        try{
            return Thread.currentThread().getStackTrace()[1].getMethodName();
        } catch (Exception e){
            return ApiConstants.UNDEFINED;
        }
    }
}
