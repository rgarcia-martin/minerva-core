package com.fractalmindstudio.minerva_core.identity.user.interfaces.rest;

import com.fractalmindstudio.minerva_core.identity.user.application.UserService;
import com.fractalmindstudio.minerva_core.identity.user.domain.Role;
import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(UserController.BASE_PATH)
@Validated
public class UserController {

    public static final String BASE_PATH = "/api/v1/users";

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody final CreateUserRequest request) {
        final User user = userService.create(
                request.name(), request.lastName(), request.email(),
                request.password(), request.address(), request.roles()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{userId}")
    public UserResponse getById(@PathVariable final UUID userId) {
        return toResponse(userService.getById(userId));
    }

    @PutMapping("/{userId}")
    public UserResponse update(
            @PathVariable final UUID userId,
            @Valid @RequestBody final UpdateUserRequest request
    ) {
        final User user = userService.update(
                userId, request.name(), request.lastName(), request.email(),
                request.password(), request.address(), request.roles(), request.active()
        );
        return toResponse(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(final User user) {
        return new UserResponse(
                user.id(), user.name(), user.lastName(), user.email(),
                user.address(), user.roles(), user.active()
        );
    }

    public record CreateUserRequest(
            @NotBlank String name,
            @NotBlank String lastName,
            @NotBlank String email,
            @NotBlank String password,
            String address,
            @NotEmpty Set<Role> roles
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String name,
            @NotBlank String lastName,
            @NotBlank String email,
            @NotBlank String password,
            String address,
            @NotEmpty Set<Role> roles,
            @NotNull Boolean active
    ) {
    }

    public record UserResponse(
            UUID id,
            String name,
            String lastName,
            String email,
            String address,
            Set<Role> roles,
            boolean active
    ) {
    }
}
