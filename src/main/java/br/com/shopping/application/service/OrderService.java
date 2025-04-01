package br.com.shopping.application.service;

import br.com.shopping.application.dto.*;
import br.com.shopping.domain.entity.Order;
import br.com.shopping.domain.entity.OrderItem;
import br.com.shopping.domain.entity.Product;
import br.com.shopping.domain.entity.User;
import br.com.shopping.domain.exception.ProductNotFoundException;
import br.com.shopping.domain.exception.StockInsufficientException;
import br.com.shopping.domain.repository.OrderRepository;
import br.com.shopping.domain.repository.ProductRepository;
import br.com.shopping.infrastructure.kafka.producer.OrderEventProducer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderDTO createOrder(CreateOrderDTO createOrderDTO) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Order order = Order.builder()
                .user(currentUser)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        
        List<String> outOfStockProducts = new ArrayList<>();
        
        for (CreateOrderDTO.OrderItemRequestDTO itemDTO : createOrderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado: " + itemDTO.getProductId()));
            
            if (product.getStock() < itemDTO.getQuantity()) {
                outOfStockProducts.add(product.getName());
                continue;
            }
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice())
                    .build();
            
            order.addItem(orderItem);
        }
        
        if (!outOfStockProducts.isEmpty()) {
            throw new StockInsufficientException("Produtos sem estoque suficiente: " + String.join(", ", outOfStockProducts));
        }
        
        Order savedOrder = orderRepository.save(order);
        
        publishOrderCreatedEvent(savedOrder);
        
        return mapToDTO(savedOrder);
    }
    
    @Transactional
    public OrderDTO processPayment(Long orderId, PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));
        

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Pedido não está pendente de pagamento");
        }
        
        if (paymentDTO.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new RuntimeException("Valor do pagamento não corresponde ao valor do pedido");
        }
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                throw new StockInsufficientException("Produto sem estoque suficiente: " + product.getName());
            }
        }
        

        order.setStatus(Order.OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(order);
        
        publishOrderPaidEvent(updatedOrder);
        
        return mapToDTO(updatedOrder);
    }
    
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
        
        return mapToDTO(order);
    }
    
    public List<OrderDTO> getOrdersByCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Order> orders = orderRepository.findByUser(currentUser);
        
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(item -> OrderItemDTO.builder()
                                .id(item.getId())
                                .productId(item.getProduct().getId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
    
    private void publishOrderCreatedEvent(Order order) {
        OrderEventDTO eventDTO = createOrderEventDTO(order);
        orderEventProducer.sendOrderCreatedEvent(eventDTO);
    }
    
    private void publishOrderPaidEvent(Order order) {
        OrderEventDTO eventDTO = createOrderEventDTO(order);
        orderEventProducer.sendOrderPaidEvent(eventDTO);
    }
    
    private OrderEventDTO createOrderEventDTO(Order order) {
        return OrderEventDTO.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(item -> OrderEventDTO.OrderItemEventDTO.builder()
                                .productId(item.getProduct().getId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}