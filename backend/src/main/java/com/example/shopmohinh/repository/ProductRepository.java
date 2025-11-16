package com.example.shopmohinh.repository;

import com.example.shopmohinh.dto.projection.ProductProjection;
import com.example.shopmohinh.dto.search.ProductSearch;
import com.example.shopmohinh.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query(value = """
            select * from product order by product.id desc limit 1
            """,nativeQuery = true)
    Product getTop1();

    @Query(value = """
                    select p.id as 'id',
                           p.code as 'code',
                           p.DESCRIPTION as 'description',
                           p.HEIGHT as 'height',
                           p.NAME as 'name',
                           p.PRICE as 'price',
                           p.QUANTITY as 'quantity',
                           p.WEIGHT as 'weight',
                           p.STATUS as 'status',
                           i.is_main as 'mainImage',
                           i.image_url as 'imageUrl',
                           i.id as 'idImage'
                           from product p
                           left join image i on p.ID = i.product_id and i.is_main = true
                           where (:#{#request.code} is null or p.code like :#{#request.code}% )
                                 and (:#{#request.name} is null or p.name like %:#{#request.name}% )
                                 and (:#{#request.price} is null or p.price = :#{#request.price} )
                                 and (:#{#request.weight} is null or p.WEIGHT = :#{#request.weight} )
                                 and (:#{#request.height} is null or p.HEIGHT = :#{#request.height} )
                           order by p.CREATED_DATE desc     
            """, nativeQuery = true)
    Page<ProductProjection> getAll(ProductSearch request, Pageable pageable);

    Optional<Product> existsProductById(Long id);

    Optional<Product> findByCode(String code);
}
