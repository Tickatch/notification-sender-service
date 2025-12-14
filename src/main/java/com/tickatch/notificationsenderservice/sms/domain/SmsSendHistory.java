package com.tickatch.notificationsenderservice.sms.domain;

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
@Table(name = "p_sms_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsSendHistory extends AbstractTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String phoneNumber;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SmsSendStatus status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @Column(columnDefinition = "TEXT")
  private String senderResponse;

  private LocalDateTime sentAt;

  public static SmsSendHistory create(String phoneNumber, String content) {
    SmsSendHistory history = new SmsSendHistory();

    history.phoneNumber = Objects.requireNonNull(phoneNumber);
    history.content = Objects.requireNonNull(content);
    history.status = SmsSendStatus.PENDING;

    return history;
  }

  public void markAsSuccess(String senderResponse) {
    this.status = SmsSendStatus.SUCCESS;
    this.sentAt = LocalDateTime.now();
    this.senderResponse = senderResponse;
  }

  public void markAsFailed(String errorMessage) {
    this.status = SmsSendStatus.FAILED;
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
    SmsSendHistory history = (SmsSendHistory) o;
    return getId() != null && Objects.equals(getId(), history.getId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id);
  }
}
