package br.com.shopping.infrastructure.kafka.consumer;

import br.com.shopping.application.dto.OrderEventDTO;
import br.com.shopping.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "order.paid", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderPaidEvent(OrderEventDTO event) {
        log.info("Received order paid event for order id: {}", event.getOrderId());
        
        event.getItems().forEach(item -> {
            try {
                productService.updateProductStock(item.getProductId(), item.getQuantity());
                log.info("Successfully updated stock for product id: {}", item.getProductId());
            } catch (Exception e) {
                log.error("Failed to update stock for product id: {}", item.getProductId(), e);
            }
        });
    }
}