package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;

public class OrganisationTest {

    @Test
    void shouldCreateOrganisationAsActive(){
        Organisation organisation = new Organisation("TestOrg", 1L);
        assertTrue(organisation.isActive());
    }

    @Test
    void shouldSuspendOrganisation(){
        Organisation organisation = new Organisation("TestOrg", 1L);
        organisation.suspend();
        assertFalse(organisation.isActive());
    }

    @Test
    void shouldNotSuspendAlreadySuspendedOrganisation(){
        Organisation organisation = new Organisation("TestOrg", 1L);
        organisation.suspend();
        assertThrows(CustomUnauthorizedEntityActionException.class, () -> {
            organisation.suspend();
        });
    }
}
