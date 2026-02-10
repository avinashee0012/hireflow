package com.avinashee0012.hireflow.config.security;

import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.exception.CustomOrganisationSuspendedException;
import com.avinashee0012.hireflow.repository.OrganisationRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrganisationAccessGuard {

    private final OrganisationRepo organisationRepo;

    public void ensureActiveOrganisation(User user) {
        if (user.getOrganisationId() == null) {
            return;
        }

        Organisation org = organisationRepo.findById(user.getOrganisationId())
                .orElseThrow(() -> new IllegalStateException("Organisation not found"));

        if (!org.isActive()) {
            throw new CustomOrganisationSuspendedException();
        }
    }
}
