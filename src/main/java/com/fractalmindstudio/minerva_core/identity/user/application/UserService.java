package com.fractalmindstudio.minerva_core.identity.user.application;

import com.fractalmindstudio.minerva_core.identity.user.domain.Role;
import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import com.fractalmindstudio.minerva_core.identity.user.domain.UserRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    public static final String RESOURCE_NAME = "user";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(
            final UserRepository userRepository,
            final PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public User create(
            final String name,
            final String lastName,
            final String email,
            final String password,
            final String address,
            final Set<Role> roles
    ) {
        return userRepository.save(User.create(name, lastName, email, passwordHasher.hash(password), address, roles));
    }

    public User getById(final UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User update(
            final UUID id,
            final String name,
            final String lastName,
            final String email,
            final String password,
            final String address,
            final Set<Role> roles,
            final boolean active
    ) {
        getById(id);
        return userRepository.save(new User(
                id,
                name,
                lastName,
                email,
                passwordHasher.hash(password),
                address,
                roles,
                active
        ));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        userRepository.deleteById(id);
    }
}
