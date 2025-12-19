package com.tickatch.notificationsenderservice.slack.infrastructure;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Slack API 호출 시 필요한 공통 설정을 제공하는 구성 클래스.
 *
 * <p>Slack FeignClient 요청에 Authorization 헤더를 자동으로 추가하여 Slack Bot Token 기반의 인증을 수행하도록 설정한다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@Configuration
public class SlackFeignConfig {

  @Value("${slack.bot.token}")
  private String slackToken;

  /**
   * Slack API 요청 시 Authorization 헤더를 자동으로 추가하는 인터셉터를 생성한다.
   *
   * <p>모든 FeignClient 요청에 Slack Bot Token을 포함하여 인증이 필요한 Slack API 호출이 정상적으로 이루어지도록 구성한다.
   *
   * @return Slack API 인증용 RequestInterceptor
   */
  @Bean
  public RequestInterceptor slackApiInterceptor() {
    return requestTemplate -> {
      requestTemplate.header("Authorization", "Bearer " + slackToken);
    };
  }
}
