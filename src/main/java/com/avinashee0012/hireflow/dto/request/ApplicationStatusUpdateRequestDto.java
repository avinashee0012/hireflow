package com.avinashee0012.hireflow.dto.request;

import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusUpdateRequestDto {
    @NotNull
    private ApplicationStatus status;
}
