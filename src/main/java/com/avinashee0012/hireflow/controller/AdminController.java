package com.avinashee0012.hireflow.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.service.AdminService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('SUPPORT', 'ORGADMIN')")
public class AdminController{

    private final AdminService adminService;

    @GetMapping("/users") public ResponseEntity<Page<UserResponseDto>> getUsers(
            @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(adminService.getAllUsers(page));
    }

    @PatchMapping("/users/{id}/activate") public ResponseEntity<Void> activateUser(@PathVariable Long id){
        adminService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/deactivate") public ResponseEntity<Void> deactivateUser(@PathVariable Long id){
        adminService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/roles") public ResponseEntity<UserResponseDto> updateRoles(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequestDto request){
        return ResponseEntity.ok(adminService.updateRoles(id, request));
    }
}
