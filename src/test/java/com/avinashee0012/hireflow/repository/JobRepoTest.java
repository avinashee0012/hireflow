package com.avinashee0012.hireflow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.enums.JobStatus;

@DataJpaTest
@ActiveProfiles("test")
class JobRepoTest{

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test void shouldFindJobWithinOrganisation(){
        Job job = persistJob(1L, 10L);

        Optional<Job> result = jobRepo.findByIdAndOrganisationId(job.getId(), 1L);

        assertThat(result).isPresent();
    }

    @Test void shouldNotReturnJobFromDifferentOrganisation(){
        Job job = persistJob(1L, 10L);

        Optional<Job> result = jobRepo.findByIdAndOrganisationId(job.getId(), 2L);

        assertThat(result).isEmpty();
    }

    @Test void shouldReturnTrueWhenRecruiterOwnsJob(){
        Job job = persistJob(1L, 10L);

        boolean exists = jobRepo.existsByIdAndAssignedRecruiterId(job.getId(), 10L);

        assertThat(exists).isTrue();
    }

    @Test void shouldReturnFalseWhenRecruiterDoesNotOwnJob(){
        Job job = persistJob(1L, 10L);
        boolean exists = jobRepo.existsByIdAndAssignedRecruiterId(job.getId(), 11L);

        assertThat(exists).isFalse();
    }

    @Test void shouldFindJobForAssignedRecruiter(){
        Job job = persistJob(1L, 10L);
        Optional<Job> result = jobRepo.findByIdAndAssignedRecruiterId(job.getId(), 10L);

        assertThat(result).isPresent();
    }

    @Test void shouldNotReturnJobForDifferentRecruiter(){
        Job job = persistJob(1L, 10L);

        Optional<Job> result = jobRepo.findByIdAndAssignedRecruiterId(job.getId(), 99L);

        assertThat(result).isEmpty();
    }

    @Test void shouldReturnPaginatedJobsForRecruiter(){
        for (int i = 0; i < 15; i++){
            persistJob(1L, 200L);
        }

        Page<Job> page = jobRepo.findByAssignedRecruiterId(200L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
    }

    @Test void shouldReturnOnlyJobsFromSameOrganisation(){
        persistJob(1L, 200L);
        persistJob(2L, 200L);

        Page<Job> page = jobRepo.findByOrganisationId(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOrganisationId()).isEqualTo(1L);
    }

    @Test void shouldFilterJobsByStatus(){
        persistJob(1L, 10L);
        Job job = persistJob(1L, 10L);
        job.close();
        
        Page<Job> result = jobRepo.findByJobStatus(JobStatus.OPEN, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getJobStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test void shouldReturnJobIdsForRecruiter(){
        Job job1 = persistJob(1L, 500L);
        Job job2 = persistJob(1L, 500L);

        List<Long> ids = jobRepo.findJobIdsByAssignedRecruiterId(500L);

        assertThat(ids).containsExactlyInAnyOrder(job1.getId(), job2.getId());
    }

    @Test void shouldNotReturnJobsFromOtherRecruiters(){
        persistJob(1L, 500L);
        persistJob(1L, 600L);

        List<Long> ids = jobRepo.findJobIdsByAssignedRecruiterId(500L);

        assertThat(ids).hasSize(1);
    }

    // HELPER METHOD
    private Job persistJob(Long organisationId, Long recruiterId){
        Job job = new Job("TestJob", "Test description.", organisationId, recruiterId);
        return entityManager.persistAndFlush(job);
    }
}
