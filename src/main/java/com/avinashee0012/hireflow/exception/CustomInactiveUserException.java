package com.avinashee0012.hireflow.exception;

public class CustomInactiveUserException extends RuntimeException{
    public CustomInactiveUserException(){
        super("User account is inactive");
    }
}
