package com.avinashee0012.hireflow.config.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.repository.RoleRepo;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner setupDatabase(RoleRepo roleRepo) {
        return args -> {
            setupRoles(roleRepo);
        };
    }

    private void setupRoles(RoleRepo roleRepo) {
        System.out.println("######## Setting up roles");
        if (!roleRepo.existsByName("CANDIDATE"))
            roleRepo.save(new Role("CANDIDATE"));
        if (!roleRepo.existsByName("ORGADMIN"))
            roleRepo.save(new Role("ORGADMIN"));
        if (!roleRepo.existsByName("RECRUITER"))
            roleRepo.save(new Role("RECRUITER"));
        if (!roleRepo.existsByName("SUPPORT"))
            roleRepo.save(new Role("SUPPORT"));
        System.out.println("######## Roles setup done");
    }
}
