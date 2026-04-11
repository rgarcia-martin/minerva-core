package com.fractalmindstudio.minerva_core.identity.user.application;

import com.fractalmindstudio.minerva_core.identity.user.domain.Role;
import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import com.fractalmindstudio.minerva_core.identity.user.domain.UserRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserHashingPassword() {
        when(passwordHasher.hash("pass")).thenReturn("hashed-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = userService.create("John", "Doe", "John@Test.com", "pass", "123 St", Set.of(Role.READ));

        assertThat(result.name()).isEqualTo("John");
        assertThat(result.active()).isTrue();
        assertThat(result.id()).isNotNull();
        assertThat(result.passwordHash()).isEqualTo("hashed-pass");
        assertThat(result.email()).isEqualTo("john@test.com");

        final var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().passwordHash()).isEqualTo("hashed-pass");
    }

    @Test
    void shouldGetUserById() {
        final var user = User.create("John", "Doe", "john@test.com", "hashed-pass", null, Set.of(Role.READ));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        final var result = userService.getById(user.id());

        assertThat(result).isEqualTo(user);
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        final var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var anna = User.create("Anna", "Adams", "anna@test.com", "hash1", null, Set.of(Role.READ));
        final var bob = User.create("Bob", "Brown", "bob@test.com", "hash2", null, Set.of(Role.READ));
        when(userRepository.findAll()).thenReturn(List.of(anna, bob));

        final var result = userService.findAll();

        assertThat(result).extracting(User::name).containsExactly("Anna", "Bob");
    }

    @Test
    void shouldUpdateUserHashingPassword() {
        final var id = UUID.randomUUID();
        final var existing = new User(id, "Old", "Name", "old@test.com", "old-hash", null, Set.of(Role.READ), true);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordHasher.hash("newpass")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = userService.update(id, "Jane", "Doe", "jane@test.com", "newpass", "456 Ave", Set.of(Role.EDIT), false);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Jane");
        assertThat(result.active()).isFalse();
        assertThat(result.passwordHash()).isEqualTo("new-hash");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentUser() {
        final var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, "N", "L", "e@e.com", "p", null, Set.of(Role.READ), true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteUser() {
        final var id = UUID.randomUUID();
        final var user = new User(id, "John", "Doe", "j@t.com", "hash", null, Set.of(Role.READ), true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.delete(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentUser() {
        final var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
