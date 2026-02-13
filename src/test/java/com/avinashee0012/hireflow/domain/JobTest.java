package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.enums.JobStatus;

public class JobTest {

    @Test
    void newJobShouldBeOpen() {
        Job job = new Job("Title", "Desc", 1L, 2L);
        assertEquals(JobStatus.OPEN, job.getJobStatus());
    }

    @Test
    void shouldCloseJob() {
        Job job = new Job("Title", "Desc", 1L, 2L);
        job.close();
        assertEquals(JobStatus.CLOSED, job.getJobStatus());
    }

    @Test
    void shouldNotCloseAlreadyClosedJob() {
        Job job = new Job("Title", "Desc", 1L, 2L);
        job.close();
        assertThrows(IllegalStateException.class, job::close);
    }
}
