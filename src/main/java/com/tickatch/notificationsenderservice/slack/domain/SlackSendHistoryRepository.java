package com.tickatch.notificationsenderservice.slack.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SlackSendHistoryRepository extends Repository<SlackSendHistory, Long> {

  SlackSendHistory save(SlackSendHistory history);

  Optional<SlackSendHistory> findById(Long id);

  @Query(
      """
    SELECT sh FROM SlackSendHistory sh
    WHERE (
        :keyword IS NULL OR (
            upper(sh.slackUserId) LIKE CONCAT('%', upper(:keyword), '%')
            OR upper(sh.channelId) LIKE CONCAT('%', upper(:keyword), '%')
            OR upper(sh.errorMessage) LIKE CONCAT('%', upper(:keyword), '%')
        )
    )
    AND (:type IS NULL OR sh.messageType = :type)
    AND (:status IS NULL OR sh.status = :status)
    AND ( :startDate IS NULL OR sh.createdAt >= :startDate )
    AND ( :endDate IS NULL OR sh.createdAt <= :endDate )
  """)
  Page<SlackSendHistory> search(
      @Param("type") SlackMessageType type,
      @Param("status") SlackSendStatus status,
      @Param("keyword") String keyword,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
