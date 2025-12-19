package com.tickatch.notificationsenderservice.mobile.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MobileSendHistoryRepository extends Repository<MobileSendHistory, Long> {

  MobileSendHistory save(MobileSendHistory history);

  Optional<MobileSendHistory> findById(Long id);

  @Query(
      """
    SELECT sh FROM MobileSendHistory sh
    WHERE (
        :keyword IS NULL OR (
            upper(sh.phoneNumber) LIKE CONCAT('%', upper(:keyword), '%')
            OR upper(sh.senderResponse) LIKE CONCAT('%', upper(:keyword), '%')
            OR upper(sh.errorMessage) LIKE CONCAT('%', upper(:keyword), '%')
        )
    )
    AND (:status IS NULL OR sh.status = :status)
    AND ( :startDate IS NULL OR sh.createdAt >= :startDate )
    AND ( :endDate IS NULL OR sh.createdAt <= :endDate )
  """)
  Page<MobileSendHistory> search(
      @Param("status") MobileSendStatus status,
      @Param("keyword") String keyword,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
