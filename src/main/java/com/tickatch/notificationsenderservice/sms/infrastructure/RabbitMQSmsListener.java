package com.tickatch.notificationsenderservice.sms.infrastructure;

import com.tickatch.notificationsenderservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationsenderservice.sms.application.SmsHistoryService;
import com.tickatch.notificationsenderservice.sms.domain.SmsSendHistory;
import com.tickatch.notificationsenderservice.sms.domain.SmsSender;
import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendException;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSmsListener {

  private final SmsHistoryService smsHistoryService;

  private final SmsSender smsSender;

  private final MessageResolver messageResolver;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_SMS)
  public void smsSendRequest(IntegrationEvent request) {
    EventContext.run(request, this::sendMessageAndRecordHistory);
  }

  private void sendMessageAndRecordHistory(IntegrationEvent event) {
    SmsSendRequestEvent payload = event.getPayloadAs(SmsSendRequestEvent.class);

    log.info("SMS 전송 요청 수신 - SMS[to: {}] 전송 시작", payload.getPhoneNumber());

    SmsSendHistory history =
        smsHistoryService.createHistory(
            payload.getNotificationId(), payload.getPhoneNumber(), payload.getMessage());

    String response = send(payload, history.getId());

    smsHistoryService.markAsSuccess(history.getId(), response);
  }

  private String send(SmsSendRequestEvent payload, Long historyId) {
    try {
      String response =
          smsSender.send(new SmsSendRequest(payload.getPhoneNumber(), payload.getMessage()));

      log.info("SMS[to: {}] 전송 완료", payload.getPhoneNumber());

      return response;
    } catch (SmsSendException e) {
      log.error("SMS[to: {}] 전송 실패", payload.getPhoneNumber(), e);

      smsHistoryService.markAsFailed(
          historyId, messageResolver.resolve(e.getCode(), e.getErrorArgs()));

      throw e;
    }
  }
}
