package com.avinashee0012.hireflow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.avinashee0012.hireflow.domain.entity.Role;

@DataJpaTest
@ActiveProfiles("test")
class RoleRepoTest{

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test void shouldFindRoleByName(){
        persistRole("RECRUITER");

        Optional<Role> result = roleRepo.findByName("RECRUITER");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("RECRUITER");
    }

    @Test void shouldReturnEmptyWhenRoleNotFound(){
        Optional<Role> result = roleRepo.findByName("UNKNOWN");

        assertThat(result).isEmpty();
    }

    @Test void shouldReturnTrueWhenRoleExists(){
        persistRole("ORGADMIN");

        boolean exists = roleRepo.existsByName("ORGADMIN");

        assertThat(exists).isTrue();
    }

    @Test void shouldReturnFalseWhenRoleDoesNotExist(){
        boolean exists = roleRepo.existsByName("SUPPORT");

        assertThat(exists).isFalse();
    }

    @Test void shouldBeCaseSensitiveByDefault(){
        persistRole("RECRUITER");

        Optional<Role> result = roleRepo.findByName("recruiter");

        assertThat(result).isEmpty();
    }

    @Test void shouldThrowExceptionWhenRoleNameDuplicated(){
        persistRole("CANDIDATE");

        Role duplicate = new Role("CANDIDATE");

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // HELPER METHOD
    private Role persistRole(String name){
        Role role = new Role(name);
        return entityManager.persistAndFlush(role);
    }
}
