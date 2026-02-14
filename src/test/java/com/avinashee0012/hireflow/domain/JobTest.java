package com.avinashee0012.hireflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.enums.JobStatus;

public class JobTest {

    private Job job;

    @BeforeEach
    void setup(){
        job = new Job("Title", "Desc", 1L, 2L);
    }

    @Test
    void newJobShouldBeOpen() {
        assertEquals(JobStatus.OPEN, job.getJobStatus());
    }

    @Test
    void shouldCloseJob() {
        job.close();
        assertEquals(JobStatus.CLOSED, job.getJobStatus());
    }

    @Test
    void shouldNotCloseAlreadyClosedJob() {
        job.close();
        assertThrows(IllegalStateException.class, job::close);
    }

    @Test
    void shouldReopenClosedJob() {
        job.close();
        job.reopen();
        assertEquals(JobStatus.OPEN, job.getJobStatus());
    }

    @Test
    void shouldNotReopenAlreadyOpenJob() {
        assertThrows(IllegalStateException.class, job::reopen);
    }
}
