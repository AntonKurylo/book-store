package mate.academy.repository.order;

import java.util.Optional;
import mate.academy.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItem oi "
            + "INNER JOIN oi.order o "
            + "WHERE o.id = :orderId AND o.user.id = :userId")
    Page<OrderItem> findAllByOrderIdAndUserId(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT oi FROM OrderItem oi "
            + "INNER JOIN oi.order o "
            + "WHERE oi.id = :orderItemId "
            + "AND o.id = :orderId AND o.user.id = :userId")
    Optional<OrderItem> findByIdAndOrderIdAndUserId(
            @Param("orderItemId") Long orderItemId,
            @Param("orderId") Long orderId,
            @Param("userId") Long userId);
}
