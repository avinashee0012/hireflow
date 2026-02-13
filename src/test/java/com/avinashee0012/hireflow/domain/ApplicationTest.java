package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;

public class ApplicationTest {

    @Test
    void shouldShortlistApplication() {
        Application app = new Application(1L, 2L, 3L);
        app.shortlist();
        assertEquals(ApplicationStatus.SHORTLISTED, app.getApplicationStatus());
    }

    @Test
    void shouldRejectApplication() {
        Application app = new Application(1L, 2L, 3L);
        app.reject();
        assertEquals(ApplicationStatus.REJECTED, app.getApplicationStatus());
    }
}