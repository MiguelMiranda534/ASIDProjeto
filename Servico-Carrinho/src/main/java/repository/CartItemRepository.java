package repository;

import com.ijse.bookstore.entity.CartItem;
import com.ijse.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    
    @Transactional
    @Modifying
    @Query(value = "ALTER TABLE cart_item AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    List<CartItem> findByUser(User user);

    List<CartItem> findByUser_Username(String username);
}
