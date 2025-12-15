package com.tickatch.notificationsenderservice.global.infrastructure;

import io.github.tickatch.common.util.JsonUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 클래스.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

  @Value("${messaging.email.exchange:tickatch.email}")
  private String emailExchange;

  @Value("${messaging.sms.exchange:tickatch.sms}")
  private String smsExchange;

  @Value("${messaging.slack.exchange:tickatch.slack}")
  private String slackExchange;

  public static final String QUEUE_EMAIL = "tickatch.email.queue";

  public static final String QUEUE_SMS = "tickatch.sms.queue";

  public static final String QUEUE_SLACK = "tickatch.slack.queue";

  public static final String ROUTING_KEY_EMAIL = "email.send";

  public static final String ROUTING_KEY_SMS = "sms.send";

  public static final String ROUTING_KEY_SLACK = "slack.send";

  /**
   * 이메일 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return 이메일 이벤트용 Exchange
   */
  @Bean
  public TopicExchange emailExchange() {
    return ExchangeBuilder.topicExchange(emailExchange).durable(true).build();
  }

  /**
   * 이메일 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return 이메일 큐
   */
  @Bean
  public Queue emailQueue() {
    return QueueBuilder.durable(QUEUE_EMAIL)
        .withArgument("x-dead-letter-exchange", emailExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_EMAIL)
        .build();
  }

  /**
   * 이메일 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param emailQueue 이메일 큐
   * @param emailExchange 이메일 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
    return BindingBuilder.bind(emailQueue).to(emailExchange).with(ROUTING_KEY_EMAIL);
  }

  /**
   * 이메일 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterEmailExchange() {
    return ExchangeBuilder.topicExchange(emailExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 이메일 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterEmailQueue() {
    return QueueBuilder.durable(QUEUE_EMAIL + ".dlq").build();
  }

  /**
   * 이메일 Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterEmailQueue 이메일 DLQ 큐
   * @param deadLetterEmailExchange 이메일 DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterEmailBinding(
      Queue deadLetterEmailQueue, TopicExchange deadLetterEmailExchange) {
    return BindingBuilder.bind(deadLetterEmailQueue)
        .to(deadLetterEmailExchange)
        .with("dlq." + ROUTING_KEY_EMAIL);
  }

  /**
   * SMS 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return SMS 이벤트용 Exchange
   */
  @Bean
  public TopicExchange smsExchange() {
    return ExchangeBuilder.topicExchange(smsExchange).durable(true).build();
  }

  /**
   * SMS 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return SMS 큐
   */
  @Bean
  public Queue smsQueue() {
    return QueueBuilder.durable(QUEUE_SMS)
        .withArgument("x-dead-letter-exchange", smsExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_SMS)
        .build();
  }

  /**
   * SMS 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param smsQueue SMS 큐
   * @param smsExchange SMS 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding smsBinding(Queue smsQueue, TopicExchange smsExchange) {
    return BindingBuilder.bind(smsQueue).to(smsExchange).with(ROUTING_KEY_SMS);
  }

  /**
   * SMS 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterSmsExchange() {
    return ExchangeBuilder.topicExchange(smsExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 SMS 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterSmsQueue() {
    return QueueBuilder.durable(QUEUE_SMS + ".dlq").build();
  }

  /**
   * SMS Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterSmsQueue SMS DLQ 큐
   * @param deadLetterSmsExchange SMS DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterSmsBinding(
      Queue deadLetterSmsQueue, TopicExchange deadLetterSmsExchange) {
    return BindingBuilder.bind(deadLetterSmsQueue)
        .to(deadLetterSmsExchange)
        .with("dlq." + ROUTING_KEY_SMS);
  }

  /**
   * Slack 관련 이벤트를 처리하는 Topic Exchange 설정.
   *
   * @return Slack 이벤트용 Exchange
   */
  @Bean
  public TopicExchange slackExchange() {
    return ExchangeBuilder.topicExchange(slackExchange).durable(true).build();
  }

  /**
   * Slack 메시지 큐를 생성한다.
   *
   * <p>DLX 설정을 포함하여 실패 시 Dead Letter Queue로 이동할 수 있도록 구성한다.
   *
   * @return Slack 큐
   */
  @Bean
  public Queue slackQueue() {
    return QueueBuilder.durable(QUEUE_SLACK)
        .withArgument("x-dead-letter-exchange", slackExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_SLACK)
        .build();
  }

  /**
   * Slack 관련 메시지를 큐와 Exchange에 바인딩한다.
   *
   * @param slackQueue Slack 큐
   * @param slackExchange Slack 이벤트 처리 Exchange
   * @return 바인딩 객체
   */
  @Bean
  public Binding slackBinding(Queue slackQueue, TopicExchange slackExchange) {
    return BindingBuilder.bind(slackQueue).to(slackExchange).with(ROUTING_KEY_SLACK);
  }

  /**
   * Slack 이벤트 Dead Letter Exchange 설정.
   *
   * @return Dead Letter Exchange
   */
  @Bean
  public TopicExchange deadLetterSlackExchange() {
    return ExchangeBuilder.topicExchange(slackExchange + ".dlx").durable(true).build();
  }

  /**
   * Dead Letter 상태의 Slack 메시지를 처리하기 위한 큐 생성.
   *
   * @return Dead Letter Queue
   */
  @Bean
  public Queue deadLetterSlackQueue() {
    return QueueBuilder.durable(QUEUE_SLACK + ".dlq").build();
  }

  /**
   * Slack Dead Letter 메시지를 처리하기 위한 바인딩 설정.
   *
   * @param deadLetterSlackQueue Slack DLQ 큐
   * @param deadLetterSlackExchange Slack DLX Exchange
   * @return DLQ 바인딩 객체
   */
  @Bean
  public Binding deadLetterSlackBinding(
      Queue deadLetterSlackQueue, TopicExchange deadLetterSlackExchange) {
    return BindingBuilder.bind(deadLetterSlackQueue)
        .to(deadLetterSlackExchange)
        .with("dlq." + ROUTING_KEY_SLACK);
  }

  /**
   * 메시지 직렬화/역직렬화를 위한 JSON 변환기 설정.
   *
   * @return JSON 메시지 컨버터
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  /**
   * RabbitMQ 메시지 송신을 위한 템플릿 설정.
   *
   * @param connectionFactory RabbitMQ 연결 팩토리
   * @param jsonMessageConverter 메시지 직렬화 컨버터
   * @return 구성된 RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }
}
