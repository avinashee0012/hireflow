package com.avinashee0012.hireflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobRequestDto {

    @NotBlank
    @Size(min = 5, max = 100)
    private String title;

    @NotBlank
    @Size(max = 3000)
    private String description;
}
