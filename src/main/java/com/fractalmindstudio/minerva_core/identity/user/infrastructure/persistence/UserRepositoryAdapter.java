package com.fractalmindstudio.minerva_core.identity.user.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import com.fractalmindstudio.minerva_core.identity.user.domain.UserRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(final SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(final User user) {
        return toDomain(springDataUserRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(final UUID id) {
        return springDataUserRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAllByOrderByLastNameAscNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataUserRepository.deleteById(UuidMapper.toString(id));
    }

    private UserEntity toEntity(final User user) {
        final UserEntity entity = new UserEntity();
        entity.setId(UuidMapper.toString(user.id()));
        entity.setName(user.name());
        entity.setLastName(user.lastName());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setAddress(user.address());
        entity.setRoles(user.roles());
        entity.setActive(user.active());
        return entity;
    }

    private User toDomain(final UserEntity entity) {
        return new User(
                UuidMapper.fromString(entity.getId()),
                entity.getName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getAddress(),
                entity.getRoles(),
                entity.isActive()
        );
    }
}
