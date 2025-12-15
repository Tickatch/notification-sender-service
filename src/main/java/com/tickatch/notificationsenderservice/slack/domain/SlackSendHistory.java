package com.tickatch.notificationsenderservice.slack.domain;

import com.tickatch.notificationsenderservice.global.domain.AbstractTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(name = "p_slack_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackSendHistory extends AbstractTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SlackMessageType messageType;

  private String slackUserId;

  private String channelId;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SlackSendStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  private LocalDateTime sentAt;

  public static SlackSendHistory createDm(String slackUserId, String message) {
    SlackSendHistory history = new SlackSendHistory();

    history.messageType = SlackMessageType.DM;
    history.slackUserId = slackUserId;
    history.message = message;
    history.status = SlackSendStatus.PENDING;

    return history;
  }

  public static SlackSendHistory createChannel(String channelId, String message) {
    SlackSendHistory history = new SlackSendHistory();

    history.messageType = SlackMessageType.CHANNEL;
    history.channelId = channelId;
    history.message = message;
    history.status = SlackSendStatus.PENDING;

    return history;
  }

  public void markAsSuccess() {
    this.status = SlackSendStatus.SUCCESS;
    this.sentAt = LocalDateTime.now();
  }

  public void markAsFailed(String errorMessage) {
    this.status = SlackSendStatus.FAILED;
    this.errorMessage = errorMessage;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    SlackSendHistory history = (SlackSendHistory) o;
    return getId() != null && Objects.equals(getId(), history.getId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id);
  }
}
