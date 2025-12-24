# notification-sender-service

## 개요
이 프로젝트는 Tickatch의 알림 발송 서비스 프로젝트입니다.
Notification Sender Service는 이메일, SMS, MMS, Slack 등 다양한 채널을 통해 실제 알림을 발송하고, 발송 이력을 추적합니다.

> 🚧 **MVP 단계** - 현재 핵심 기능 개발 중입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 17+ |
| Database | PostgreSQL |
| Messaging | RabbitMQ |
| Email | Spring Mail (SMTP) |
| SMS/MMS | SOLAPI |
| Slack | Spring Cloud OpenFeign |
| Retry | Spring Retry |

## 아키텍처

### 시스템 구성

```
┌──────────────────────────────────────────────────────────────┐
│                   Notification Sender Service                │
├─────────────┬─────────────┬─────────────┬────────────────────┤
│    Email    │    Slack    │   SMS/MMS   │    RabbitMQ        │
│   Sender    │   Sender    │   Sender    │    Listener        │
└──────┬──────┴──────┬──────┴──────┬──────┴──────┬─────────────┘
       │             │             │             │
       ▼             ▼             ▼             ▼
    SMTP          Slack API      SOLAPI       Event Bus
  (Gmail)       (Feign Client)              (RabbitMQ)
```

### 레이어 구조

```
notification-sender-service
├── global                  # 공통
│   ├── infrastructure
│   └── domain
├── email                   # 이메일 발송
│   ├── application
│   ├── domain
│   └── infrastructure
├── slack                   # Slack 발송
│   ├── application
│   ├── domain
│   └── infrastructure
└── sms                     # SMS/MMS 발송
    ├── application
    ├── domain
    └── infrastructure
```

## 주요 기능

### 1. 멀티 채널 알림 발송
다양한 채널을 통한 알림 발송을 지원하며, 각 채널별로 독립적인 발송 로직과 히스토리 관리를 제공합니다.

- **이메일**: SMTP 프로토콜을 통한 HTML/Plain Text 이메일 발송
- **Slack**: Feign Client를 통한 DM 및 채널 메시지 발송
- **SMS**: SOLAPI를 통한 단문 문자 발송
- **MMS**: SOLAPI를 통한 멀티미디어 문자 발송 (이미지 첨부 지원)

**활용 기술**: Spring Mail, Spring Cloud OpenFeign, SOLAPI SDK

### 2. 발송 이력 추적 및 상태 관리
모든 발송 시도를 추적하고 상태를 관리합니다.

- **상태 관리**: PENDING → SUCCESS/FAILED
- **에러 로깅**: 실패 시 상세한 에러 메시지 저장
- **발송 시각 기록**: 성공 시 정확한 발송 시각 기록
- **감사 추적**: 완전한 발송 이력 보관

**활용 기술**: Spring Data JPA, Hibernate

### 3. 비동기 메시지 처리
RabbitMQ를 통해 발송 요청을 비동기로 수신하고 처리합니다.

- **큐 기반 처리**: 각 채널별 전용 큐 운영
- **메시지 유실 방지**: 발송 실패 시에도 메시지 보존
- **부하 분산**: 시스템 부하를 분산하여 안정적인 발송 보장

**활용 기술**: RabbitMQ `@RabbitListener`, Event-Driven Architecture

### 4. 재시도 메커니즘
일시적인 장애에 대응하기 위한 지능형 재시도 기능을 제공합니다.

- **지수 백오프**: 재시도 간격을 점진적으로 증가
- **최대 재시도 제한**: 무한 재시도 방지
- **외부 시스템 보호**: 백오프를 통한 외부 API 부하 최소화

**재시도 설정**:
- 이메일: 30초, 2배수 (최대 3회)
- Slack: 5초, 2배수 (최대 3회)
- SMS: 10초, 1.5배수 (최대 4회)
- MMS: 10초, 1.5배수 (최대 2회)

**활용 기술**: Spring Retry `@Retryable`, `@Backoff`

### 5. Slack 통합
Feign Client를 통해 Slack API와 통합됩니다.

- **DM 발송**: 자동으로 다이렉트 메시지 채널 생성 후 발송
- **채널 메시지**: 특정 채널에 메시지 발송
- **API 응답 검증**: Slack API 응답 검증을 통한 안정적인 발송

**활용 기술**: Spring Cloud OpenFeign, Slack Web API

### 6. 이메일 발송
JavaMailSender를 활용하여 이메일을 발송합니다.

- **HTML 지원**: HTML 형식의 이메일 발송
- **인코딩**: UTF-8 인코딩 지원
- **발신자 설정**: 설정 파일을 통한 발신자 정보 관리
- **첨부 파일**: MimeMessage를 통한 첨부 파일 지원

**활용 기술**: Spring Mail, JavaMailSender, MimeMessage

### 7. SMS/MMS 발송
SOLAPI를 사용하여 문자 메시지를 발송합니다.

- **SMS**: 단문 문자 메시지 발송
- **MMS**: 이미지 첨부 가능한 멀티미디어 메시지 발송
- **Base64 이미지**: Base64 인코딩된 이미지를 MMS로 전송

**활용 기술**: SOLAPI SDK

### 8. 에러 처리 및 로깅
각 발송 단계별 상세 로그를 기록하고 예외를 처리합니다.

- **단계별 로깅**: 발송 시작, 성공, 실패 로그 기록
- **에러 메시지 해석**: MessageResolver를 통한 다국어 에러 메시지
- **히스토리 저장**: 모든 에러 정보를 DB에 영구 저장

**활용 기술**: SLF4J, Lombok `@Slf4j`, MessageResolver

## 발송 상태

| 상태 | 설명 |
|------|------|
| PENDING | 발송 대기 중 |
| SUCCESS | 발송 성공 |
| FAILED | 발송 실패 |

## 메시지 타입 (Slack)

| 타입 | 설명 |
|------|------|
| DM | 다이렉트 메시지 |
| CHANNEL | 채널 메시지 |

## API 명세

이 서비스는 REST API를 제공하지 않으며, RabbitMQ 메시지를 통해서만 통신합니다.

## 실행 방법

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

mobile:
  api:
    key: ${MOBILE_API_KEY}
    secret: ${MOBILE_API_SECRET}
    domain: ${MOBILE_API_DOMAIN}
  send:
    from: ${MOBILE_SEND_FROM}
    
slack:
  bot:
    token: ${SLACK_BOT_TOKEN}
```

### 실행

```bash
# 개발 환경
./gradlew bootRun

# 프로덕션 빌드
./gradlew clean build
java -jar build/libs/notification-sender-service-*.jar
```

## 데이터베이스 스키마

### p_email_send_history

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | PK |
| notification_id | BIGINT | 알림 ID |
| email_address | VARCHAR | 수신자 이메일 |
| subject | VARCHAR(500) | 제목 |
| content | TEXT | 내용 |
| is_html | BOOLEAN | HTML 여부 |
| status | VARCHAR | 발송 상태 |
| error_message | TEXT | 에러 메시지 |
| sent_at | TIMESTAMP | 발송 일시 |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

### p_slack_send_history

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | PK |
| notification_id | BIGINT | 알림 ID |
| message_type | VARCHAR | 메시지 타입 (DM, CHANNEL) |
| slack_user_id | VARCHAR | Slack 사용자 ID |
| channel_id | VARCHAR | 채널 ID |
| message | TEXT | 메시지 내용 |
| status | VARCHAR | 발송 상태 |
| error_message | TEXT | 에러 메시지 |
| sent_at | TIMESTAMP | 발송 일시 |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

### p_mobile_send_history

| 컬럼명             | 타입 | 설명       |
|-----------------|------|----------|
| id              | BIGINT | PK       |
| notification_id | BIGINT | 알림 ID    |
| phone_number    | VARCHAR | 수신자 전화번호 |
| content         | TEXT | 메시지 내용   |
| sender_response | TEXT | 응답 메시지   |
| status          | VARCHAR | 발송 상태    |
| error_message   | TEXT | 에러 메시지   |
| sent_at         | TIMESTAMP | 발송 일시    |
| created_at      | TIMESTAMP | 생성일시     |
| updated_at      | TIMESTAMP | 수정일시     |

## 이벤트 명세

### 수신 이벤트

#### EmailSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 1,
  "email": "user@example.com",
  "subject": "예매가 완료되었습니다",
  "content": "<html>...</html>",
  "isHtml": true
}
```

#### SlackChannelMessageSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 2,
  "channelId": "C01234567",
  "message": "새로운 예매가 발생했습니다"
}
```

#### SmsSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 3,
  "phoneNumber": "01012345678",
  "message": "예매가 완료되었습니다"
}
```

#### MmsSendRequestEvent
- **Payload**:
```json
{
  "notificationId": 4,
  "phoneNumber": "01012345678",
  "message": "티켓이 발행되었습니다",
  "imageBase64": "base64_encoded_image_data"
}
```

### 발행 이벤트

#### NotificationResultEvent
- **Payload**:
```json
{
  "notificationId": 1,
  "success": true,
  "errorMessage": null
}
```

## 외부 API 통합

### Slack API
- **Base URL**: `https://slack.com/api`
- **Endpoints**:
    - `POST /conversations.open`: DM 채널 생성
    - `POST /chat.postMessage`: 메시지 발송

### SOLAPI
- **Base URL**: `https://api.solapi.com`
- **Endpoints**:
    - `POST /messages/v4/send`: SMS/MMS 발송

## 관련 서비스

- **Notification Service** - 알림 생성 및 발송 조율

## 트러블슈팅

### SMTP 연결 실패
- **문제**: Gmail SMTP 연결 실패
- **해결**:
    1. Google 계정에서 "보안 수준이 낮은 앱 액세스" 허용
    2. 또는 앱 비밀번호 생성하여 사용

### Slack API 429 에러
- **문제**: Slack API Rate Limit 초과
- **해결**: 재시도 메커니즘의 백오프 시간 증가

### MMS 이미지 크기 초과
- **문제**: MMS 이미지 크기 제한 (300KB) 초과
- **해결**: QR 코드 이미지 압축 및 크기 최적화

### 재시도 무한 루프
- **문제**: 외부 API 장애 시 계속 재시도
- **해결**: 최대 재시도 횟수 제한 및 Circuit Breaker 패턴 고려

---

© 2025 Tickatch Team