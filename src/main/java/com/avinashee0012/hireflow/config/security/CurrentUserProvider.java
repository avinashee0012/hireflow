package com.avinashee0012.hireflow.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.exception.CustomNotLoggedInException;
import com.avinashee0012.hireflow.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CurrentUserProvider {
    
    private final UserRepo userRepo;

    public User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) throw new CustomNotLoggedInException();
        String email = authentication.getName();
        return userRepo.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Entity not found with email: " + email));
    }
}
