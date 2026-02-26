package com.avinashee0012.hireflow.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRolesRequestDto {

    @NotEmpty
    private Set<String> roles;
}