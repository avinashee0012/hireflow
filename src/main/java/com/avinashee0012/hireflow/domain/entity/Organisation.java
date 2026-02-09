package com.avinashee0012.hireflow.domain.entity;

import com.avinashee0012.hireflow.domain.enums.OrganisationStatus;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organisations")
@Getter
@NoArgsConstructor
public class Organisation extends Auditor{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganisationStatus status;

    @Column(name = "org_admin_user_id", nullable = false)
    private Long orgAdminUserId;

    public Organisation(String name, Long orgAdminUserId) {
        this.name = name;
        this.orgAdminUserId = orgAdminUserId;
        this.status = OrganisationStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status == OrganisationStatus.SUSPENDED)
            throw new CustomUnauthorizedEntityActionException("Organisation is already suspended");
        this.status = OrganisationStatus.SUSPENDED;
    }

    public void activate() {
        if (this.status == OrganisationStatus.ACTIVE)
            throw new CustomUnauthorizedEntityActionException("Organisation is already active");
        this.status = OrganisationStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == OrganisationStatus.ACTIVE;
    }

    public void changeOrgAdmin(Long newOrgAdminUserId) {
        if (newOrgAdminUserId == null)
            throw new IllegalArgumentException("ORGADMIN userId must not be null");
        this.orgAdminUserId = newOrgAdminUserId;
    }
}