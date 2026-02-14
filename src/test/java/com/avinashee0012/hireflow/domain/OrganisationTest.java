package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.enums.OrganisationStatus;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;

public class OrganisationTest {

    private Organisation organisation;

    @BeforeEach
    void setup(){
        organisation = new Organisation("TestOrg", 1L);
    }

    @Test
    void shouldCreateOrganisationAsActive(){
        assertTrue(organisation.isActive());
    }

    @Test
    void shouldSuspendOrganisation(){
        organisation.suspend();
        assertFalse(organisation.isActive());
    }

    @Test
    void shouldNotSuspendAlreadySuspendedOrganisation(){
        organisation.suspend();
        assertThrows(CustomUnauthorizedEntityActionException.class, () -> {
            organisation.suspend();
        });
    }

    @Test
    void shouldNotAllowBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Organisation("", 1L));
    }

    @Test
    void shouldNotAllowNullOrgAdminId() {
        assertThrows(IllegalArgumentException.class, () -> new Organisation("TestOrg", null));
    }

    @Test
    void shouldActivateSuspendedOrganisation() {
        organisation.suspend();
        organisation.activate();
        assertEquals(OrganisationStatus.ACTIVE, organisation.getStatus());
        assertTrue(organisation.isActive());
    }

    @Test
    void shouldNotActivateAlreadyActiveOrganisation() {
        assertThrows(CustomUnauthorizedEntityActionException.class, organisation::activate);
    }

    @Test
    void shouldChangeOrgAdmin() {
        organisation.changeOrgAdmin(2L);
        assertEquals(2L, organisation.getOrgAdminUserId());
    }

    @Test
    void shouldNotAllowNullAdminReassignment() {
        assertThrows(IllegalArgumentException.class, () -> organisation.changeOrgAdmin(null));
    }
}
