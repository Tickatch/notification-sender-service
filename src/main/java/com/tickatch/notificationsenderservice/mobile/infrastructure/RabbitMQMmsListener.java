package com.tickatch.notificationsenderservice.mobile.infrastructure;

import com.tickatch.notificationsenderservice.global.infrastructure.RabbitMQConfig;
import com.tickatch.notificationsenderservice.mobile.application.MobileHistoryService;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSendHistory;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSender;
import com.tickatch.notificationsenderservice.mobile.domain.dto.MmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendException;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import io.github.tickatch.common.message.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQMmsListener {

  private final MobileHistoryService mobileHistoryService;

  private final MobileSender mobileSender;

  private final MessageResolver messageResolver;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_MMS)
  public void mmsSendRequest(IntegrationEvent request) {
    EventContext.run(request, this::sendMessageAndRecordHistory);
  }

  private void sendMessageAndRecordHistory(IntegrationEvent event) {
    MmsSendRequestEvent payload = event.getPayloadAs(MmsSendRequestEvent.class);

    log.info("MMS 발송 요청 이벤트 수신 - MMS[to: {}] 전송 시작", payload.getPhoneNumber());

    MobileSendHistory history =
        mobileHistoryService.createHistory(
            payload.getNotificationId(), payload.getPhoneNumber(), payload.getMessage());

    sendAndRecord(payload, history.getId());
  }

  private void sendAndRecord(MmsSendRequestEvent event, Long historyId) {
    try {
      MmsSendRequest request =
          MmsSendRequest.fromBase64(
              event.getPhoneNumber(), event.getMessage(), event.getImageBase64());

      String response = mobileSender.send(request);

      log.info("MMS[to: {}] 발송 완료", event.getPhoneNumber());

      mobileHistoryService.markAsSuccess(historyId, response);
    } catch (MobileSendException e) {
      log.error("MMS[to: {}] 발송 실패", event.getPhoneNumber(), e);

      mobileHistoryService.markAsFailed(
          historyId, messageResolver.resolve(e.getCode(), e.getErrorArgs()));
    }
  }
}
