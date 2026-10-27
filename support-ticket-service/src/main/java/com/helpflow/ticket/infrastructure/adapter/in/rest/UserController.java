package com.helpflow.ticket.infrastructure.adapter.in.rest;

import com.helpflow.ticket.application.port.in.ManageUserUseCase;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.PageQuery;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.PageResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.ChangeUserStatusRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.UpdateProfileRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.UserResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UserController {

    private final ManageUserUseCase manageUserUseCase;
    private final RestMapper mapper;

    @GetMapping("/me")
    @Operation(summary = "Mi perfil")
    public UserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return mapper.toResponse(manageUserUseCase.getProfile(currentUser.id()));
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi información básica")
    public UserResponse updateMe(@AuthenticationPrincipal CurrentUser currentUser,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return mapper.toResponse(manageUserUseCase.updateProfile(currentUser.id(), mapper.toCommand(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios (ADMIN)")
    public PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(manageUserUseCase.list(new PageQuery(page, size)), mapper::toResponse);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar / inactivar un usuario (ADMIN)")
    public UserResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusRequest request) {
        return mapper.toResponse(manageUserUseCase.changeStatus(id, request.status()));
    }
}
