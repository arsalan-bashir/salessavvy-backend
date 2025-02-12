package com.spring.salessavvy.repositories;

import com.spring.salessavvy.entities.CartItems;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, Integer> {

    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItems c WHERE c.user.userId= :userId")
    int countTotalItems(int userId);

    @Query("SELECT c FROM CartItems c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<CartItems> findByUserAndProduct(int userId, int productId);

    @Query("SELECT c FROM CartItems c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
    List<CartItems> findCartItemsWithProductDetails(int userId);

    @Modifying
    @Transactional
    @Query("UPDATE CartItems c SET c.quantity = :quantity WHERE c.id = :cartItemId")
    void updateCartItemQuantity(int cartItemId,  int quantity);


    @Modifying
    @Transactional
    @Query("DELETE FROM CartItems c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteCartItem(int userId, int productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItems c WHERE c.user.userId = :userId")
    void deleteAllCartItems(int userId);
}
