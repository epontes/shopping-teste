package br.com.shopping.infrastructure.kafka.producer;

import br.com.shopping.application.dto.OrderEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEventDTO> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderEventDTO orderEvent) {
        CompletableFuture<SendResult<String, OrderEventDTO>> future = kafkaTemplate.send("order.created", String.valueOf(orderEvent.getOrderId()), orderEvent);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent order created event for order id: {}", orderEvent.getOrderId());
            } else {
                log.error("Unable to send order created event for order id: {}", orderEvent.getOrderId(), ex);
            }
        });
    }

    public void sendOrderPaidEvent(OrderEventDTO orderEvent) {
        CompletableFuture<SendResult<String, OrderEventDTO>> future = kafkaTemplate.send("order.paid", String.valueOf(orderEvent.getOrderId()), orderEvent);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent order paid event for order id: {}", orderEvent.getOrderId());
            } else {
                log.error("Unable to send order paid event for order id: {}", orderEvent.getOrderId(), ex);
            }
        });
    }
}