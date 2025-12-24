package com.tickatch.notificationsenderservice;

import com.tickatch.notificationsenderservice.email.domain.EmailSender;
import com.tickatch.notificationsenderservice.mobile.domain.MobileSender;
import com.tickatch.notificationsenderservice.slack.domain.SlackSender;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
public class SmsTest {

  private final EmailSender emailSender;
  private final MobileSender mobileSender;
  private final SlackSender slackDmSender;

  @Test
  void test() {
    //      smsSender.send(new SmsSendRequest("01097157738", "[TEXT: SMS 전송 테스트]"));
    //    smsSender.send(new SmsSendRequest("01038085972", "[TEXT: SMS 전송 테스트]"));
  }

  //
  //    @Test
  //    void emailTest() {
  //      EmailSendRequest request =
  //          new EmailSendRequest("khs96523@naver.com", "[EMAIL] 이메일 전송", "이메일 전송 테스트.", false);
  //      emailSender.send(request);
  //    }

  @Test
  void slackTest() {
    //    String userId = "U0A39KC0VLL";
    //    String chanelId = "C0A30AACGN8";
    //    String message = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "알림 메시지";
    //    slackDmSender.sendDirectMessage(new SlackDmSendRequest(userId, message));
    //    slackDmSender.sendChannelMessage(new SlackChannelSendRequest(chanelId, message));
  }
}
