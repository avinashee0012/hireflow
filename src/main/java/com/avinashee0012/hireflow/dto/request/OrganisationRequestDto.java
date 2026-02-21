package com.avinashee0012.hireflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrganisationRequestDto {

    @NotBlank
    @Size(min = 5, max = 150)
    private String name;

    @NotNull
    @Positive
    private Long adminUserId;
}