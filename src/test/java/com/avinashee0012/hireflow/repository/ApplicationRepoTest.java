package com.avinashee0012.hireflow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.avinashee0012.hireflow.domain.entity.Application;

@DataJpaTest
@ActiveProfiles("test")
public class ApplicationRepoTest{

    @Autowired
    private ApplicationRepo applicationRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test void shouldReturnTrueWhenApplicationExists(){
        persistApplication(10L, 100L, 1L);

        boolean exists = applicationRepo.existsByJobIdAndCandidateId(10L, 100L);

        assertThat(exists).isTrue();
    }

    @Test void shouldReturnFalseWhenApplicationDoesNotExist(){
        boolean exists = applicationRepo.existsByJobIdAndCandidateId(10L, 200L);

        assertThat(exists).isFalse();
    }

    @Test void shouldThrowExceptionForDuplicateApplication(){
        persistApplication(20L, 300L, 1L);

        Application duplicate = new Application(20L, 300L, 1L);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test void shouldFindApplicationForCandidate(){
        Application app = persistApplication(30L, 400L, 1L);

        Optional<Application> result = applicationRepo.findByIdAndCandidateId(app.getId(), 400L);

        assertThat(result).isPresent();
    }

    @Test void shouldNotReturnApplicationForDifferentCandidate(){
        Application app = persistApplication(30L, 400L, 1L);

        Optional<Application> result = applicationRepo.findByIdAndCandidateId(app.getId(), 999L);

        assertThat(result).isEmpty();
    }

    @Test void shouldFindApplicationWithinOrganisation(){
        Application app = persistApplication(40L, 500L, 1L);

        Optional<Application> result = applicationRepo.findByIdAndOrganisationId(app.getId(), 1L);

        assertThat(result).isPresent();
    }

    @Test void shouldNotReturnApplicationFromDifferentOrganisation(){
        Application app = persistApplication(40L, 500L, 1L);

        Optional<Application> result = applicationRepo.findByIdAndOrganisationId(app.getId(), 2L);

        assertThat(result).isEmpty();
    }

    @Test void shouldReturnPaginatedApplicationsForCandidate(){
        for (int i = 0; i < 12; i++){
            persistApplication((long) i, 600L, 1L);
        }

        Page<Application> page = applicationRepo.findByCandidateId(600L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(12);
    }

    @Test void shouldReturnOnlyApplicationsFromSameOrganisation(){
        persistApplication(1L, 1L, 1L);
        persistApplication(2L, 2L, 2L);

        Page<Application> page = applicationRepo.findByOrganisationId(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOrganisationId()).isEqualTo(1L);
    }

    @Test void shouldReturnApplicationsForGivenJobIds(){
        persistApplication(101L, 1L, 1L);
        persistApplication(102L, 2L, 1L);
        persistApplication(200L, 3L, 1L);

        Page<Application> page = applicationRepo.findByJobIdIn(List.of(101L, 102L), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
    }

    @Test void shouldReturnEmptyWhenNoJobsMatch(){
        persistApplication(300L, 1L, 1L);

        Page<Application> page = applicationRepo.findByJobIdIn(List.of(999L), PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    // HELPER METHOD
    private Application persistApplication(Long jobId, Long candidateId, Long organisationId){
        Application application = new Application(jobId, candidateId, organisationId);
        return entityManager.persistAndFlush(application);
    }
}
