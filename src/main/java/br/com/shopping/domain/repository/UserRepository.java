package br.com.shopping.domain.repository;

import br.com.shopping.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT u.id, u.email, u.full_name, COUNT(o.id) as order_count, SUM(o.total_amount) as total_spent " +
            "FROM users u " +
            "JOIN orders o ON u.id = o.user_id " +
            "WHERE o.status = 'PAID' " +
            "GROUP BY u.id " +
            "ORDER BY total_spent DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5UsersByTotalSpent();

    @Query(value = "SELECT u.id, u.email, u.full_name, AVG(o.total_amount) as average_ticket " +
            "FROM users u " +
            "JOIN orders o ON u.id = o.user_id " +
            "WHERE o.status = 'PAID' " +
            "GROUP BY u.id", nativeQuery = true)
    List<Object[]> findAverageTicketByUser();
}
