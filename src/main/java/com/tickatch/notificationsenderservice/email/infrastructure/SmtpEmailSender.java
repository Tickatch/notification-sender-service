package com.tickatch.notificationsenderservice.email.infrastructure;

import com.tickatch.notificationsenderservice.email.domain.EmailSender;
import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendRequest;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendErrorCode;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class SmtpEmailSender implements EmailSender {

  private final JavaMailSender mailSender;

  @Value("${notification-sender.email.from:tickatch1211@gmail.com}")
  private String fromEmail;

  @Value("${notification-sender.email.from-name:Tickatch}")
  private String fromName;

  @Override
  @Retryable(
      retryFor = {MailException.class},
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public void send(EmailSendRequest request) {
    MimeMessage message = createMessage(request);

    sendEmail(request, message);
  }

  private void sendEmail(EmailSendRequest request, MimeMessage message) {
    try {
      log.info("이메일 발송 시작: to={}, subject={}", request.to(), request.subject());

      mailSender.send(message);

      log.info("이메일 발송 성공: to={}", request.to());
    } catch (MailException e) {
      log.error("이메일 발송 실패: to={}", request.to(), e);
      throw new EmailSendException(EmailSendErrorCode.EMAIL_SEND_FAILED, e);
    } catch (Exception e) {
      log.error("이메일 발송 중 예상치 못한 에러: to={}", request.to(), e);
      throw new EmailSendException(EmailSendErrorCode.EMAIL_SEND_UNKNOWN, e);
    }
  }

  private MimeMessage createMessage(EmailSendRequest request) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(request.to());
      helper.setSubject(request.subject());
      helper.setText(request.content(), request.isHtml());

      return message;
    } catch (MessagingException | UnsupportedEncodingException e) {
      log.error("이메일 메시지 생성 실패: to={}", request.to(), e);
      throw new EmailSendException(EmailSendErrorCode.EMAIL_SEND_MESSAGE_CREATION_FAILED, e);
    }
  }
}
