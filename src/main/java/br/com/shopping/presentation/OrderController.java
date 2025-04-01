package br.com.shopping.presentation;

import br.com.shopping.application.dto.CreateOrderDTO;
import br.com.shopping.application.dto.OrderDTO;
import br.com.shopping.application.dto.PaymentDTO;
import br.com.shopping.application.service.OrderService;
import br.com.shopping.domain.exception.StockInsufficientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        try {
            OrderDTO orderDTO = orderService.createOrder(createOrderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
        } catch (StockInsufficientException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Stock insufficient");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order creation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processPayment(@PathVariable Long id, @RequestBody PaymentDTO paymentDTO) {
        try {
            OrderDTO orderDTO = orderService.processPayment(id, paymentDTO);
            return ResponseEntity.ok(orderDTO);
        } catch (StockInsufficientException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Stock insufficient");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Payment processing failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO orderDTO = orderService.getOrderById(id);
        return ResponseEntity.ok(orderDTO);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getCurrentUserOrders() {
        List<OrderDTO> orders = orderService.getOrdersByCurrentUser();
        return ResponseEntity.ok(orders);
    }
}