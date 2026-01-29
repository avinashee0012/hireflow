package com.avinashee0012.hireflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avinashee0012.hireflow.domain.entity.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long>{
}
