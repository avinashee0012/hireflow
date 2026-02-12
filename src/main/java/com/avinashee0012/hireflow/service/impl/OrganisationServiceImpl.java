package com.avinashee0012.hireflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.OrganisationRequestDto;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;
import com.avinashee0012.hireflow.exception.CustomDuplicateEntityException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.OrganisationMapper;
import com.avinashee0012.hireflow.repository.OrganisationRepo;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.OrganisationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrganisationServiceImpl implements OrganisationService{
    private static final Logger log = LoggerFactory.getLogger(OrganisationServiceImpl.class);

    private final OrganisationRepo organisationRepo;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final OrganisationMapper organisationMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional public OrganisationResponseDto createOrganisation(OrganisationRequestDto request){
        User actor = currentUserProvider.getAuthenticatedUser();
        if (!actor.hasRole("SUPPORT"))
            throw new CustomUnauthorizedException("Only SUPPORT can create organisations");
        if (organisationRepo.existsByName(request.getName()))
            throw new CustomDuplicateEntityException("Organisation already exists with name: " + request.getName());
        User orgAdmin = userRepo.findById(request.getAdminUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getAdminUserId()));
        if (orgAdmin.getOrganisationId() != null)
            throw new IllegalStateException("User already belongs to an organisation");
        if (!orgAdmin.hasRole("ORGADMIN")){
            Role orgAdminRole = roleRepo.findByName("ORGADMIN")
                    .orElseThrow(() -> new EntityNotFoundException("ORGADMIN Role not found"));
            orgAdmin.assignRole(orgAdminRole);
        }
        Organisation organisation = new Organisation(request.getName(), orgAdmin.getId());
        Organisation savedOrganisation = organisationRepo.save(organisation);
        orgAdmin.assignOrganisation(savedOrganisation.getId());
        log.info("Organisation created: orgId={}, name={}, orgAdminId={}", savedOrganisation.getId(),
                savedOrganisation.getName(), orgAdmin.getId());
        return organisationMapper.toResponse(savedOrganisation);
    }

    @Override
    @Transactional public void suspendOrganisation(Long orgId){
        User actor = currentUserProvider.getAuthenticatedUser();
        if (!actor.hasRole("SUPPORT"))
            throw new CustomUnauthorizedException("Only SUPPORT can suspend organisations");
        Organisation organisation = organisationRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with id: " + orgId));
        organisation.suspend();
        log.info("Organisation suspended: orgId={}", orgId);
    }

    @Override
    @Transactional public void activateOrganisation(Long orgId){
        User actor = currentUserProvider.getAuthenticatedUser();
        if (!actor.hasRole("SUPPORT"))
            throw new CustomUnauthorizedException("Only SUPPORT can activate organisations");
        Organisation organisation = organisationRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with id: " + orgId));
        organisation.activate();
        log.info("Organisation activated: orgId={}", orgId);
    }

    @Override public OrganisationResponseDto getOrganisation(Long orgId){
        User actor = currentUserProvider.getAuthenticatedUser();
        Organisation organisation = organisationRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with id: " + orgId));
        if (actor.hasRole("SUPPORT"))
            return organisationMapper.toResponse(organisation);
        if (actor.hasRole("ORGADMIN")){
            if (!orgId.equals(actor.getOrganisationId()))
                throw new CustomUnauthorizedException("ORGADMIN can access only their organisation");
            return organisationMapper.toResponse(organisation);
        }
        throw new CustomUnauthorizedException("Access denied");
    }
}
