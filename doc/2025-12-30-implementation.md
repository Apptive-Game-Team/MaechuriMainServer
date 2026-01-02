# 2025-12-30 구현 내용 문서

> 작성일: 2025-12-30

## 📝 개요

오늘 구현된 주요 기능은 **JWT 기반 대화 내용 암호화** 및 **히스토리 관리**입니다. 사용자와 NPC 간의 대화 내용을 JWT를 사용하여 안전하게 관리하고, 이를 인코딩 및 디코딩하는 로직이 포함됩니다.

---

## ✨ 주요 기능

### 1. JWT Provider (`JwtProvider.kt`)

범용적으로 사용할 수 있는 JWT 생성 및 검증 모듈입니다.

#### 주요 특징
- **RSA-256 암호화**: Public/Private Key를 사용한 비대칭키 암호화 방식을 채택하여 보안을 강화했습니다.
- **PEM 키 파일 사용**: `application.yml`에 지정된 경로에서 `.pem` 형식의 키 파일을 읽어옵니다.
- **유연한 토큰 생성**: `subject`, `claims`, `ttl` (만료 시간)을 동적으로 설정하여 토큰을 생성할 수 있습니다.

#### 주요 메소드

- `createToken(subject: String, claims: Map<String, Any>, ttl: Duration? = null): String`
  - **설명**: 주어진 정보를 바탕으로 JWT를 생성하고 Private Key로 서명합니다.
  - **예시**:
    ```kotlin
    val claims = mapOf("userId" to 1, "role" to "USER")
    val token = jwtProvider.createToken("user-auth", claims, Duration.hours(1))
    ```

- `verifyAndExtract(token: String): Map<String, Any>`
  - **설명**: 주어진 토큰을 Public Key로 검증하고, 내부에 포함된 `claims`를 추출합니다.
  - **예시**:
    ```kotlin
    val claims = jwtProvider.verifyAndExtract(token)
    val userId = claims["userId"]
    ```

#### 키 파일 처리
- `decodePublicKey`, `decodePrivateKey`, `parsePem` 메소드를 통해 PEM 형식의 문자열에서 "-----BEGIN..." 헤더와 푸터를 제거하고 Base64 디코딩을 수행하여 `PublicKey` 및 `PrivateKey` 객체를 생성합니다.

---

### 2. 대화 히스토리 관리 (`HistoryService.kt`)

`JwtProvider`를 활용하여 대화 히스토리(`ConversationHistory`)를 JWT로 변환하거나, JWT에서 다시 복원하는 서비스입니다.

#### 도메인 모델 (`ConversationHistory.kt`)

- **`ConversationHistory`**: 대화의 주체인 `objectId`와 대화 메시지 목록(`List<Message>`)을 포함하는 데이터 클래스입니다.
  ```kotlin
  class ConversationHistory(
      val objectId: Long,
      val conversation: List<Message>
  )
  ```

#### 주요 메소드

- `encodeHistory(history: ConversationHistory): String`
  - **설명**: `ConversationHistory` 객체를 직렬화하여 JWT의 `history` claim에 담아 인코딩합니다.
  - **JWT Subject**: `"history"`
  - **로직**:
    1. `ConversationHistory` 객체를 `Map`으로 변환합니다.
    2. `jwtProvider.createToken`을 호출하여 JWT를 생성합니다.

- `decodeHistory(objectId: Long, historyJwt: String): ConversationHistory`
  - **설명**: 대화 히스토리 JWT(`historyJwt`)를 디코딩하여 `ConversationHistory` 객체로 복원합니다.
  - **로직**:
    1. `jwtProvider.verifyAndExtract`를 호출하여 토큰을 검증하고 `claims`를 추출합니다.
    2. `history` claim에 담긴 JSON(Map) 데이터를 `jacksonObjectMapper`를 사용하여 `ConversationHistory` 객체로 변환합니다.
    3. 만약 `history` claim이 없다면, 빈 대화 목록을 가진 `ConversationHistory` 객체를 반환합니다.

## 🚀 기대 효과

- **보안 강화**: 대화 내용이 JWT로 암호화되어 클라이언트와 서버 간에 전달되므로, 중간에 데이터가 탈취되더라도 내용을 파악하기 어렵습니다.
- **상태 유지의 유연성**: 클라이언트가 대화 히스토리 JWT를 가지고 있다가 필요할 때 서버에 전달하면, 서버는 별도의 DB 조회 없이 대화의 연속성을 유지할 수 있습니다.
- **모듈화**: JWT 관련 로직이 `JwtProvider`로 분리되어 있어, 향후 다른 인증/인가 기능에도 재사용이 용이합니다.
