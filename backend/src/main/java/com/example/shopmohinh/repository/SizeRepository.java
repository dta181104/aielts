package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.Product;
import com.example.shopmohinh.entity.SizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<SizeEntity, Long> {
    @Transactional
    @Query(value = """
            select * from size order by size.id desc limit 1
            """,nativeQuery = true)
    SizeEntity getTop1();

    @Transactional
    Optional<SizeEntity> findByCode(String code);
}
