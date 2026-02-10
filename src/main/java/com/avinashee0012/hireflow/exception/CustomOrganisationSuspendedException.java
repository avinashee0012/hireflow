package com.avinashee0012.hireflow.exception;

public class CustomOrganisationSuspendedException extends RuntimeException{
    public CustomOrganisationSuspendedException(){
        super("Organisation is suspended");
    }
}
