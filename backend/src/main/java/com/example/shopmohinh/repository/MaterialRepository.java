package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.MaterialEntity;
import com.example.shopmohinh.entity.SizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {
    @Transactional
    @Query(value = """
            select * from material order by material.id desc limit 1
            """,nativeQuery = true)
    MaterialEntity getTop1();

    @Transactional
    Optional<MaterialEntity> findByCode(String code);
}
