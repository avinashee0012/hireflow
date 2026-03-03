package com.avinashee0012.hireflow.repository;

import static org.assertj.core.api.Assertions.*;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.avinashee0012.hireflow.domain.entity.Organisation;

@DataJpaTest
@ActiveProfiles("test")
public class OrganisationRepoTest{

    @Autowired
    private OrganisationRepo organisationRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test void shouldReturnTrueWhenOrganisationExists(){
        persistOrganisation("TestOrg");

        boolean exists = organisationRepo.existsByName("TestOrg");

        assertThat(exists).isTrue();
    }

    @Test void shouldReturnFalseWhenOrganisationDoesNotExist(){
        boolean exists = organisationRepo.existsByName("NonExistent");

        assertThat(exists).isFalse();
    }

    @Test void shouldBeCaseSensitiveByDefault(){
        persistOrganisation("TestOrg");

        boolean exists = organisationRepo.existsByName("testorg");

        assertThat(exists).isFalse();
    }

    @Test void shouldThrowExceptionWhenOrganisationNameDuplicated(){
        persistOrganisation("TestOrg");

        Organisation duplicate = new Organisation("TestOrg", 1L);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test void shouldNotMatchWithTrailingSpaces(){
        persistOrganisation("TestOrg");

        boolean exists = organisationRepo.existsByName("TestOrg ");

        assertThat(exists).isFalse();
    }

    // HELPER METHOD
    private Organisation persistOrganisation(String name){
        Organisation organisation = new Organisation(name, 1L);
        return entityManager.persistAndFlush(organisation);
    }
}
