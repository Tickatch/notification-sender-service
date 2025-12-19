package com.tickatch.notificationsenderservice.mobile.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Base64;

public record MmsSendRequest(
    @NotBlank(message = "수신자 번호는 필수입니다.") String to,
    @NotBlank(message = "내용은 필수입니다.") String content,
    @NotNull(message = "이미지는 필수입니다.") @Size(max = 200 * 1024, message = "이미지 크기는 200KB 이하여야 합니다.")
        byte[] imageData) {

  public MmsSendRequest {
    imageData = imageData.clone();
  }

  public static MmsSendRequest fromBase64(String to, String content, String base64Image) {
    String base64Data =
        base64Image.contains(",")
            ? base64Image.substring(base64Image.indexOf(",") + 1)
            : base64Image;

    byte[] imageData = Base64.getDecoder().decode(base64Data);

    return new MmsSendRequest(to, content, imageData);
  }
}
