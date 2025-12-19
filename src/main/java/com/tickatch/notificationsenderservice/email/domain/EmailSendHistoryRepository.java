package com.tickatch.notificationsenderservice.email.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface EmailSendHistoryRepository extends Repository<EmailSendHistory, Long> {

  EmailSendHistory save(EmailSendHistory history);

  Optional<EmailSendHistory> findById(Long id);

  @Query(
      """
    SELECT eh FROM EmailSendHistory eh
    WHERE (
        :keyword IS NULL OR (
            upper(eh.emailAddress) LIKE CONCAT('%', upper(:keyword), '%')
            OR upper(eh.errorMessage) LIKE CONCAT('%', upper(:keyword), '%')
        )
    )
    AND (:status IS NULL OR eh.status = :status)
    AND ( :startDate IS NULL OR eh.createdAt >= :startDate )
    AND ( :endDate IS NULL OR eh.createdAt <= :endDate )
  """)
  Page<EmailSendHistory> search(
      @Param("status") EmailSendStatus status,
      @Param("keyword") String keyword,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
