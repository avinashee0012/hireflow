package com.avinashee0012.hireflow.domain.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends Auditor{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "organisation_id")
    private Long organisationId; // nullable by design

    public User(String email, String encrptedPassword, Long organisationId){
        this.email = email;
        this.password = encrptedPassword;
        this.organisationId = organisationId;
    }

    public boolean hasRole(String roleName){
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public void assignRole(Role role){
        if (role == null)
            throw new IllegalArgumentException("Role cannot be null");
        roles.add(role);
    }

    public void removeRole(Role role){
        if (role == null)
            throw new IllegalArgumentException("Role cannot be null");
        if (roles.size() == 1)
            throw new IllegalStateException("User must have at least one role");
        roles.remove(role);
    }

    public void activate(){
        if (active)
            throw new IllegalStateException("User is already active");
        active = true;
    }

    public void deactivate(){
        if (!active)
            throw new IllegalStateException("User is already inactive");
        active = false;
    }

    public void changePassword(String encryptedPassword){
        if (encryptedPassword == null || encryptedPassword.isBlank())
            throw new IllegalArgumentException("Password must not be null or blank");
        this.password = encryptedPassword;
    }

    public void assignOrganisation(Long organisationId){
        if(this.organisationId != null)
            throw new IllegalStateException("User already belongs to an organisation");
        this.organisationId = organisationId;
    }

}