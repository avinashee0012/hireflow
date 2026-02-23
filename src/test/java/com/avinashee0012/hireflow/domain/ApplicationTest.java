package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;

public class ApplicationTest {

    private Application application;

    @BeforeEach
    void setup(){
        application = new Application(1L, 2L, 3L);
    }

    @Test
    void shouldCreateApplicationWithAppliedStatus() {
        assertEquals(ApplicationStatus.APPLIED,
                application.getApplicationStatus());
    }

    @Test
    void shouldShortlistApplication() {
        application.shortlist();
        assertEquals(ApplicationStatus.SHORTLISTED, application.getApplicationStatus());
    }

    @Test
    void shouldRejectApplication() {
        application.reject();
        assertEquals(ApplicationStatus.REJECTED, application.getApplicationStatus());
    }

    @Test
    void shouldWithdrawApplication() {
        application.withdraw();
        assertEquals(ApplicationStatus.WITHDRAWN, application.getApplicationStatus());
    }

    @Test
    void shouldNotWithdrawTwice() {
        application.withdraw();
        assertThrows(CustomUnauthorizedEntityActionException.class, application::withdraw);
    }

    @Test
    void shouldNotShortlistWithdrawnApplication() {
        application.withdraw();
        assertThrows(CustomUnauthorizedEntityActionException.class, application::shortlist);
    }

    @Test
    void shouldNotRejectAlreadyRejectedApplication() {
        application.reject();
        assertThrows(CustomUnauthorizedEntityActionException.class, application::reject);
    }

    @Test
    void shouldNotModifyAfterShortlisted() {
        application.shortlist();
        assertThrows(CustomUnauthorizedEntityActionException.class, application::withdraw);
    }

    @Test
    void shouldNotModifyAfterRejected() {
        application.reject();
        assertThrows(CustomUnauthorizedEntityActionException.class, application::shortlist);
    }
}