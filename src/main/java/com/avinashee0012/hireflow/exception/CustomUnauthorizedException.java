package com.avinashee0012.hireflow.exception;

public class CustomUnauthorizedException extends RuntimeException{
    public CustomUnauthorizedException(){
        super("Unauthorized - Role permissions");
    }
}
