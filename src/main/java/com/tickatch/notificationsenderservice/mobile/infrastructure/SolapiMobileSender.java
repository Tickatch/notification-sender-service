package com.tickatch.notificationsenderservice.mobile.infrastructure;

import com.tickatch.notificationsenderservice.mobile.domain.MobileSender;
import com.tickatch.notificationsenderservice.mobile.domain.dto.MmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.dto.SmsSendRequest;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendErrorCode;
import com.tickatch.notificationsenderservice.mobile.domain.exception.MobileSendException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoBadRequestException;
import net.nurigo.sdk.message.exception.NurigoInvalidApiKeyException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
public class SolapiMobileSender implements MobileSender {

  private final String from;
  private final DefaultMessageService messageService;

  public SolapiMobileSender(
      @Value("${mobile.api.key}") String apiKey,
      @Value("${mobile.api.secret}") String apiSecret,
      @Value("${mobile.api.domain}") String domain,
      @Value("${mobile.send.from}") String from) {
    this.from = from;
    this.messageService = new DefaultMessageService(apiKey, apiSecret, domain);
  }

  @Override
  @Retryable(
      retryFor = {MobileSendException.class},
      maxAttempts = 4,
      backoff = @Backoff(delay = 10000, multiplier = 1.5, maxDelay = 40000))
  public String send(SmsSendRequest request) {
    Message message = createSms(request);

    return sendSms(request, message);
  }

  @Override
  @Retryable(
      retryFor = {MobileSendException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 10000, multiplier = 1.5, maxDelay = 30000))
  public String send(MmsSendRequest request) {
    File tempFile = null;

    try {
      log.info("MMS 발송 시작: to={}, imageSize={}bytes", request.to(), request.imageData().length);

      // 1. 임시 파일 생성
      tempFile = createTempImageFile(request.imageData());

      // 2. 이미지 업로드 (Solapi 스토리지)
      String imageId = uploadImage(tempFile);

      // 3. MMS 메시지 생성
      Message message = createMms(request, imageId);

      // 4. MMS 발송
      SingleMessageSentResponse response =
          messageService.sendOne(new SingleMessageSendingRequest(message));

      log.info("MMS 발송 성공: to={}, messageId={}", request.to(), response.getMessageId());

      return response.getStatusMessage();

    } catch (Exception e) {
      log.error("MMS 발송 실패: to={}", request.to(), e);
      handleSendException(e);
      return null;

    } finally {
      // 5. 임시 파일 정리
      deleteTempFile(tempFile);
    }
  }

  private Message createSms(SmsSendRequest request) {
    Message message = new Message();

    message.setType(MessageType.SMS);
    message.setFrom(from);
    message.setTo(request.to());
    message.setText(request.content());

    return message;
  }

  private String sendSms(SmsSendRequest request, Message message) {
    try {
      log.info("SMS 발송 시작: to={}", request.to());

      SingleMessageSentResponse response =
          messageService.sendOne(new SingleMessageSendingRequest(message));

      log.info("SMS 발송 성공: to={}", request.to());

      return response.getStatusMessage();
    } catch (Exception e) {
      log.error("SMS 발송 실패: to={}", request.to(), e);
      handleSendException(e);
      return null;
    }
  }

  private Message createMms(MmsSendRequest request, String imageId) {
    Message message = new Message();

    message.setType(MessageType.MMS);
    message.setFrom(from);
    message.setTo(request.to());
    message.setText(request.content());
    message.setImageId(imageId); // 업로드된 이미지 ID 설정

    return message;
  }

  /** 이미지를 Solapi 스토리지에 업로드 */
  private String uploadImage(File imageFile) throws IOException {
    try {
      log.debug("이미지 업로드 시작: file={}, size={}bytes", imageFile.getName(), imageFile.length());

      String imageId = messageService.uploadFile(imageFile, StorageType.MMS, null);

      log.debug("이미지 업로드 완료: imageId={}", imageId);

      return imageId;

    } catch (Exception e) {
      log.error("이미지 업로드 실패: file={}", imageFile.getName());
      throw new MobileSendException(MobileSendErrorCode.MMS_IMAGE_UPLOAD_FAILED, e);
    }
  }

  /** 바이트 배열을 임시 파일로 저장 */
  private File createTempImageFile(byte[] imageData) throws IOException {
    try {
      // 임시 디렉토리에 파일 생성
      Path tempDir = Files.createTempDirectory("mms-");
      String fileName = UUID.randomUUID().toString() + ".jpg";
      File tempFile = tempDir.resolve(fileName).toFile();

      // 바이트 배열을 파일에 쓰기
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write(imageData);
      }

      log.debug(
          "임시 이미지 파일 생성: path={}, size={}bytes", tempFile.getAbsolutePath(), imageData.length);

      return tempFile;

    } catch (IOException e) {
      log.error("임시 파일 생성 실패");
      throw new MobileSendException(MobileSendErrorCode.MMS_TEMP_FILE_CREATION_FAILED, e);
    }
  }

  /** 임시 파일 삭제 */
  private void deleteTempFile(File file) {
    if (file != null && file.exists()) {
      try {
        Files.deleteIfExists(file.toPath());

        // 부모 디렉토리도 삭제 시도
        Path parentDir = file.toPath().getParent();
        if (parentDir != null && Files.isDirectory(parentDir)) {
          Files.deleteIfExists(parentDir);
        }

        log.debug("임시 파일 삭제 완료: path={}", file.getAbsolutePath());

      } catch (IOException e) {
        log.warn("임시 파일 삭제 실패 (무시): path={}", file.getAbsolutePath(), e);
      }
    }
  }

  private void handleSendException(Exception e) {
    switch (e) {
      case NurigoBadRequestException be ->
          throw new MobileSendException(MobileSendErrorCode.SMS_SEND_FAILED, be);
      case NurigoInvalidApiKeyException ie ->
          throw new MobileSendException(MobileSendErrorCode.MOBILE_INVALID_API_KEY, ie);
      case NurigoUnknownException ue ->
          throw new MobileSendException(MobileSendErrorCode.SMS_SEND_UNKNOWN, ue);
      case MobileSendException me -> throw me;
      default -> throw new MobileSendException(MobileSendErrorCode.SMS_SEND_UNKNOWN, e);
    }
  }
}
