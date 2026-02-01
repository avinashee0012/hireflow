package com.avinashee0012.hireflow.exception;

public class CustomUnauthorizedEntityActionException extends RuntimeException{
    public CustomUnauthorizedEntityActionException(String message){
        super("Unauthorized action: " + message);
    }
}
