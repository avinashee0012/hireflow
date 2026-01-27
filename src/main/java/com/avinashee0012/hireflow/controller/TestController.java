package com.avinashee0012.hireflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping
    public String testApi(){
        return "<div style=\"text-align:center; color: green; font-size: 20px; font-weight: bold; margin-top: 50px;\">HireFlow API: All good in here!</div>";
    }
}
