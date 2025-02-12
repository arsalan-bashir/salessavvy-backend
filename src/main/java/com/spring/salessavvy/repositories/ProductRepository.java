package com.spring.salessavvy.repositories;

import com.spring.salessavvy.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory_CategoryId(int categoryId);

    @Query("SELECT p.category.categoryName FROM Product p WHERE p.productId= :productId")
    String findByCategoryNameByProductId(int productId);


}
