package com.tickatch.notificationsenderservice.sms.infrastructure;

import com.tickatch.notificationsenderservice.sms.domain.SmsSender;
import com.tickatch.notificationsenderservice.sms.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendErrorCode;
import com.tickatch.notificationsenderservice.sms.domain.exception.SmsSendException;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoBadRequestException;
import net.nurigo.sdk.message.exception.NurigoInvalidApiKeyException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
public class SolapiSmsSender implements SmsSender {

  private final String from;
  private final DefaultMessageService messageService;

  public SolapiSmsSender(
      @Value("${sms.api.key}") String apiKey,
      @Value("${sms.api.secret}") String apiSecret,
      @Value("${sms.api.domain}") String domain,
      @Value("${sms.send.from}") String from) {
    this.from = from;
    this.messageService = new DefaultMessageService(apiKey, apiSecret, domain);
  }

  @Override
  public String send(SmsSendRequest request) {
    Message message = createMessage(request);

    return sendSms(request, message);
  }

  private Message createMessage(SmsSendRequest request) {
    Message message = new Message();

    message.setType(MessageType.SMS);
    message.setFrom(from);
    message.setTo(request.to());
    message.setText(request.content());

    return message;
  }

  private String sendSms(SmsSendRequest request, Message message) {
    try {
      log.info("SMS 발송 시작: to={}", request.to());

      SingleMessageSentResponse response =
          messageService.sendOne(new SingleMessageSendingRequest(message));

      log.info("SMS 발송 성공: to={}", request.to());

      return response.getStatusMessage();
    } catch (Exception e) {
      log.error("SMS 발송 실패: to={}", request.to(), e);
      switch (e) {
        case NurigoBadRequestException be ->
            throw new SmsSendException(SmsSendErrorCode.SMS_SEND_FAILED, be);
        case NurigoInvalidApiKeyException ie ->
            throw new SmsSendException(SmsSendErrorCode.SMS_INVALID_API_KEY, ie);
        case NurigoUnknownException ue ->
            throw new SmsSendException(SmsSendErrorCode.SMS_SEND_UNKNOWN, ue);
        default -> throw new SmsSendException(SmsSendErrorCode.SMS_SEND_UNKNOWN, e);
      }
    }
  }
}
