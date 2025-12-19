package com.tickatch.notificationsenderservice.mobile.infrastructure;

import com.tickatch.notificationsenderservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationsenderservice.mobile.application.MobileHistoryService;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSender;
import com.tickatch.notificationsenderservice.mobile.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendException;
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

  private final MobileHistoryService mobileHistoryService;

  private final MobileSender mobileSender;

  private final MessageResolver messageResolver;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_SMS)
  public void smsSendRequest(IntegrationEvent request) {
    EventContext.run(request, this::sendMessageAndRecordHistory);
  }

  private void sendMessageAndRecordHistory(IntegrationEvent event) {
    SmsSendRequestEvent payload = event.getPayloadAs(SmsSendRequestEvent.class);

    log.info("SMS 전송 요청 수신 - SMS[to: {}] 전송 시작", payload.getPhoneNumber());

    MobileSendHistory history =
        mobileHistoryService.createHistory(
            payload.getNotificationId(), payload.getPhoneNumber(), payload.getMessage());

    sendAndRecord(payload, history.getId());
  }

  private void sendAndRecord(SmsSendRequestEvent payload, Long historyId) {
    try {
      String response =
          mobileSender.send(new SmsSendRequest(payload.getPhoneNumber(), payload.getMessage()));

      log.info("SMS[to: {}] 전송 완료", payload.getPhoneNumber());

      mobileHistoryService.markAsSuccess(historyId, response);
    } catch (MobileSendException e) {
      log.error("SMS[to: {}] 전송 실패", payload.getPhoneNumber(), e);

      mobileHistoryService.markAsFailed(
          historyId, messageResolver.resolve(e.getCode(), e.getErrorArgs()));
    }
  }
}
