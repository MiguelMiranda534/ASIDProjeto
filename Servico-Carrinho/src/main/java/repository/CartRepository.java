package repository;

import com.ijse.bookstore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository <Cart , Long>{

    Cart getCartIdByUserId(Long userId);

}