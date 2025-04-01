package br.com.shopping.domain.repository;

import br.com.shopping.domain.entity.Order;
import br.com.shopping.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserAndStatus(User user, Order.OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'PAID' AND o.updatedAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT SUM(o.total_amount) FROM orders o " +
            "WHERE o.status = 'PAID' " +
            "AND YEAR(o.updated_at) = YEAR(CURRENT_DATE()) " +
            "AND MONTH(o.updated_at) = MONTH(CURRENT_DATE())", nativeQuery = true)
    BigDecimal calculateTotalRevenueForCurrentMonth();
}