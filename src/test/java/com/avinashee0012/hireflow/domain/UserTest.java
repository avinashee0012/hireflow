package com.avinashee0012.hireflow.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Role recruiterRole;
    private Role orgAdminRole;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "encryptedPass", null);
        recruiterRole = new Role("RECRUITER");
        orgAdminRole = new Role("ORGADMIN");
    }

    @Test
    void shouldAssignRole() {
        user.assignRole(recruiterRole);
        assertTrue(user.getRoles().contains(recruiterRole));
    }

    @Test
    void shouldThrowWhenAssigningNullRole() {
        assertThrows(IllegalArgumentException.class, () -> user.assignRole(null));
    }

    @Test
    void shouldRemoveRoleWhenMoreThanOneExists() {
        user.assignRole(recruiterRole);
        user.assignRole(orgAdminRole);
        user.removeRole(recruiterRole);
        assertFalse(user.getRoles().contains(recruiterRole));
    }

    @Test
    void shouldNotRemoveLastRole() {
        user.assignRole(recruiterRole);
        assertThrows(IllegalStateException.class,
                () -> user.removeRole(recruiterRole));
    }

    @Test
    void shouldDeactivateActiveUser() {
        user.deactivate();
        assertFalse(user.isActive());
    }

    @Test
    void shouldThrowWhenDeactivatingInactiveUser() {
        user.deactivate();
        assertThrows(IllegalStateException.class,
                user::deactivate);
    }

    @Test
    void shouldActivateInactiveUser() {
        user.deactivate();
        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    void shouldChangePassword() {
        user.changePassword("newEncryptedPass");
        assertEquals("newEncryptedPass", user.getPassword());
    }

    @Test
    void shouldThrowWhenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> user.changePassword(""));
    }

    @Test
    void shouldAssignOrganisation() {
        user.assignOrganisation(10L);
        assertEquals(10L, user.getOrganisationId());
    }

    @Test
    void shouldNotReassignOrganisation() {
        user.assignOrganisation(10L);
        assertThrows(IllegalStateException.class,
                () -> user.assignOrganisation(20L));
    }
}

