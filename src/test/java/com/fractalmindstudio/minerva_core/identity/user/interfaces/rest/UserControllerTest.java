package com.fractalmindstudio.minerva_core.identity.user.interfaces.rest;

import com.fractalmindstudio.minerva_core.identity.user.application.UserService;
import com.fractalmindstudio.minerva_core.identity.user.domain.Role;
import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import com.fractalmindstudio.minerva_core.shared.interfaces.rest.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String BASE_PATH = UserController.BASE_PATH;

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        final var user = User.create("John", "Doe", "john@test.com", "secret123", "123 St", Set.of(Role.READ));
        when(userService.create(eq("John"), eq("Doe"), eq("john@test.com"), eq("secret123"), eq("123 St"), any()))
                .thenReturn(user);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John", "lastName": "Doe", "email": "john@test.com",
                                    "password": "secret123", "address": "123 St", "roles": ["READ"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        final var users = List.of(
                User.create("A", "B", "a@b.com", "pass", null, Set.of(Role.READ))
        );
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetUserById() throws Exception {
        final var user = User.create("John", "Doe", "john@test.com", "pass", null, Set.of(Role.READ));
        when(userService.getById(user.id())).thenReturn(user);

        mockMvc.perform(get(BASE_PATH + "/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = new User(id, "Jane", "Doe", "jane@test.com", "newpass", "456 Ave", Set.of(Role.EDIT), true);
        when(userService.update(eq(id), eq("Jane"), eq("Doe"), eq("jane@test.com"), eq("newpass"), eq("456 Ave"), any(), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Jane", "lastName": "Doe", "email": "jane@test.com",
                                    "password": "newpass", "address": "456 Ave", "roles": ["EDIT"], "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(userService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(userService.getById(id)).thenThrow(new NotFoundException("user", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("user with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "", "lastName": "Doe", "email": "a@b.com",
                                    "password": "pass", "roles": ["READ"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenEmailIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John", "lastName": "Doe",
                                    "password": "pass", "roles": ["READ"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenRolesIsEmpty() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John", "lastName": "Doe", "email": "a@b.com",
                                    "password": "pass", "roles": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenUpdateMissingActiveField() throws Exception {
        final var id = UUID.randomUUID();
        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John", "lastName": "Doe", "email": "a@b.com",
                                    "password": "pass", "roles": ["READ"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenBodyIsNotReadable() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenPathVariableIsInvalidUuid() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn415WhenMediaTypeIsUnsupported() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void shouldReturn400WhenServiceThrowsIllegalArgument() throws Exception {
        when(userService.create(eq("John"), eq("Doe"), eq("john@test.com"), eq("pass"), isNull(), any()))
                .thenThrow(new IllegalArgumentException("invalid user"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John", "lastName": "Doe", "email": "john@test.com",
                                    "password": "pass", "roles": ["READ"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid user"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(userService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
