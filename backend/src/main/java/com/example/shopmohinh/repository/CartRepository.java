package com.example.shopmohinh.repository;

import com.example.shopmohinh.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    @Query(value = "SELECT * FROM cart ORDER BY id DESC LIMIT 1", nativeQuery = true)
    CartEntity getTop1();

    @Query(value = """
                    select * from cart c
            inner join user u on c.user_id = u.ID
            where u.USERNAME = :username
            """, nativeQuery = true)
    CartEntity findByUsername(String username);


}
