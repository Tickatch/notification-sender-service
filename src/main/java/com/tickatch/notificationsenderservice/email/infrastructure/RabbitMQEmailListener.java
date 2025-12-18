package com.tickatch.notificationsenderservice.email.infrastructure;

import com.tickatch.notificationsenderservice.email.application.EmailHistoryService;
import com.tickatch.notificationsenderservice.email.domain.EmailSendHistory;
import com.tickatch.notificationsenderservice.email.domain.EmailSender;
import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendRequest;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendException;
import com.tickatch.notificationsenderservice.global.infrastructure.RabbitMQConfig;
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
public class RabbitMQEmailListener {

  private final EmailHistoryService emailHistoryService;

  private final EmailSender emailSender;

  private final MessageResolver messageResolver;

  @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
  public void emailSendRequest(IntegrationEvent request) {
    EventContext.run(request, this::sendEmailAndRecordHistory);
  }

  private void sendEmailAndRecordHistory(IntegrationEvent event) {
    EmailSendRequestEvent payload = event.getPayloadAs(EmailSendRequestEvent.class);

    log.info("이메일 전송 요청 수신 - 이메일[to: {}] 전송 시작", payload.getEmail());

    EmailSendHistory history =
        emailHistoryService.createHistory(
            payload.getNotificationId(),
            payload.getEmail(),
            payload.getSubject(),
            payload.getContent(),
            payload.isHtml());

    send(payload, history.getId());

    emailHistoryService.markAsSuccess(history.getId());
  }

  private void send(EmailSendRequestEvent payload, Long historyId) {
    try {
      emailSender.send(
          new EmailSendRequest(
              payload.getEmail(), payload.getSubject(), payload.getContent(), payload.isHtml()));

      log.info("이메일[to: {}] 전송 완료", payload.getEmail());
    } catch (EmailSendException e) {
      log.error("이메일[to: {}] 전송 실패", payload.getEmail(), e);

      emailHistoryService.markAsFailed(
          historyId, messageResolver.resolve(e.getCode(), e.getErrorArgs()));

      throw e;
    }
  }
}
