package com.avinashee0012.hireflow.exception;

public class CustomDuplicateEntityException extends RuntimeException{
    public CustomDuplicateEntityException(String message){
        super(message);
    }
}
