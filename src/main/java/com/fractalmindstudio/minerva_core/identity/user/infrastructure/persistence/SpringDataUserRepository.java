package com.fractalmindstudio.minerva_core.identity.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, String> {

    List<UserEntity> findAllByOrderByLastNameAscNameAsc();
}
