package com.tickatch.notificationsenderservice.global.infrastructure;

import io.github.tickatch.common.event.DomainEvent;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendResultPublisher {

  @Value("${spring.application.name:notification-service}")
  private String serviceName;

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.notification-sender:tickatch.notification-sender}")
  private String emailExchange;

  public void publish(SendResultEvent event) {
    log.info("알림[{}] 발송 결과 전송 이벤트 발행", event.getNotificationId());

    publishEvent(event, event.getNotificationId());
  }

  private void publishEvent(DomainEvent event, Long notificationId) {
    log.info("알림[{}] 발송 결과 전송 이벤트 발행 시작: {}", notificationId, event);

    IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);

    try {
      rabbitTemplate.convertAndSend(emailExchange, event.getRoutingKey(), integrationEvent);
      log.info(
          "알림[{}] 발송 결과 전송 이벤트 발행 완료: exchange={}, routingKey={}",
          notificationId,
          emailExchange,
          event.getRoutingKey());
    } catch (AmqpException e) {
      log.error(
          "알림[{}] 발송 결과 전송 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          notificationId,
          emailExchange,
          event.getRoutingKey(),
          event,
          e);
      throw e;
    }
  }
}
