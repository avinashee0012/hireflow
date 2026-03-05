package com.avinashee0012.hireflow.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.avinashee0012.hireflow.domain.entity.User;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepoTest{

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test void shouldFindUserByEmail(){
        persistUser("john@test.com", 1L);

        Optional<User> result = userRepo.findByEmail("john@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test void shouldReturnEmptyWhenEmailNotFound(){
        Optional<User> result = userRepo.findByEmail("missing@test.com");

        assertThat(result).isEmpty();
    }

    @Test void shouldReturnTrueWhenEmailExists(){
        persistUser("exists@test.com", 1L);

        boolean result = userRepo.existsByEmail("exists@test.com");

        assertThat(result).isTrue();
    }

    @Test void shouldReturnFalseWhenEmailDoesNotExist(){
        boolean result = userRepo.existsByEmail("nope@test.com");

        assertThat(result).isFalse();
    }

    @Test void shouldReturnOnlyUsersFromGivenOrganisation(){
        persistUser("org1@test.com", 1L);
        persistUser("org2@test.com", 2L);

        Page<User> page = userRepo.findByOrganisationId(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getOrganisationId()).isEqualTo(1L);
    }

    @Test void shouldRespectPaginationAndTotalCount(){
        for (int i = 0; i < 15; i++){
            persistUser("user" + i + "@test.com", 1L);
        }

        Page<User> page = userRepo.findByOrganisationId(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test void paginationShouldNotLeakOtherOrganisationUsers(){
        for (int i = 0; i < 5; i++){
            persistUser("org1_" + i + "@test.com", 1L);
        }

        for (int i = 0; i < 20; i++){
            persistUser("org2_" + i + "@test.com", 2L);
        }

        Page<User> page = userRepo.findByOrganisationId(1L, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).allMatch(user -> user.getOrganisationId().equals(1L));
    }

    @Test void shouldThrowExceptionWhenEmailIsDuplicated(){
        persistUser("dup@test.com", 1L);

        User duplicate = new User("dup@test.com", "encoded-password", 1L);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // HELPER METHOD
    private User persistUser(String email, Long organisationId){
        User user = new User(email, "encoded-password", organisationId);
        return entityManager.persistAndFlush(user);
    }
}
