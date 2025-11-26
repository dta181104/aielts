package com.example.shopmohinh.repository;

import com.example.shopmohinh.dto.response.RoleResponse;
import com.example.shopmohinh.entity.Role;
import com.example.shopmohinh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findRoleByCode(String code);
    List<Role> findAllByCodeIn(List<String> codes);
}
