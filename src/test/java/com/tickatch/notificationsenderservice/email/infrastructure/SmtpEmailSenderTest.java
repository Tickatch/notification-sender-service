package com.tickatch.notificationsenderservice.email.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tickatch.notificationsenderservice.email.domain.dto.EmailSendRequest;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendErrorCode;
import com.tickatch.notificationsenderservice.email.domain.exception.EmailSendException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmtpEmailSender 테스트")
class SmtpEmailSenderTest {

  @Mock private JavaMailSender mailSender;

  @Mock private MimeMessage mimeMessage;

  @InjectMocks private SmtpEmailSender emailSender;

  EmailSendRequest request;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailSender, "fromEmail", "tickatch1211@gmail.com");
    ReflectionTestUtils.setField(emailSender, "fromName", "Tickatch");

    request = new EmailSendRequest("test@example.com", "테스트 제목", "테스트 내용", false);
  }

  @Test
  void send() {
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);
    doNothing().when(mailSender).send(any(MimeMessage.class));

    emailSender.send(request);

    verify(mailSender, times(1)).createMimeMessage();
    verify(mailSender, times(1)).send(mimeMessage);
  }

  @Test
  void sendHtmlEmail() {
    EmailSendRequest request =
        new EmailSendRequest("test@example.com", "HTML 테스트", "<h1>HTML 내용</h1>", true);

    given(mailSender.createMimeMessage()).willReturn(mimeMessage);
    doNothing().when(mailSender).send(any(MimeMessage.class));

    emailSender.send(request);

    verify(mailSender, times(1)).send(mimeMessage);
  }

  @Test
  void sendMailExceptionThrowsEmailSendException() {
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);
    doThrow(new MailSendException("메일 발송 실패")).when(mailSender).send(any(MimeMessage.class));

    assertThatThrownBy(() -> emailSender.send(request))
        .isInstanceOf(EmailSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", EmailSendErrorCode.EMAIL_SEND_FAILED);

    verify(mailSender, times(1)).send(mimeMessage);
  }

  @Test
  void sendUnexpectedExceptionThrowsEmailSendException() {
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);
    doThrow(new RuntimeException("예상치 못한 에러")).when(mailSender).send(any(MimeMessage.class));

    assertThatThrownBy(() -> emailSender.send(request))
        .isInstanceOf(EmailSendException.class)
        .hasFieldOrPropertyWithValue("errorCode", EmailSendErrorCode.EMAIL_SEND_UNKNOWN);
  }
}
