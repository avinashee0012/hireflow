package com.avinashee0012.hireflow.exception;

public class CustomNotLoggedInException extends RuntimeException{
    public CustomNotLoggedInException(){
        super("Not logged-in");
    }
}
