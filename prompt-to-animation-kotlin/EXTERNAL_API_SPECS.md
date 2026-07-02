# 외부 API 연동 규격서 (Prompt-to-Animation Generator)

이 문서는 본 과제에서 연동해야 하는 외부 AI 모델(OpenAI, Kie.ai)의 API 스펙과 통신 방식을 명세합니다.

---

## 1. OpenAI (Text-to-Scene Planning)
사용자의 자연어 프롬프트를 입력받아 구체적인 씬(Scene) 기획안과 컷(Cut) 프롬프트로 분할 및 변환합니다.

- **Model ID**: `gpt-5.4-mini` (과제 요구사항에 따른 권장 모델)
- **인증**: `Authorization: Bearer YOUR_OPENAI_API_KEY`
- **Base URL**: `https://api.openai.com/v1`
- **엔드포인트**: `POST /responses`

### 1.1 주요 연동 전략 (Structured Outputs with Responses API)
비즈니스 로직(Kie API 연동)과의 원활한 파이프라인 구축을 위해 OpenAI의 최신 **Responses API 기반 Structured Outputs** 기능을 활용합니다.
- `text.format` 속성에 `json_schema`를 강제하여, 지정된 스키마 구조(JSON)로만 응답을 반환하도록 설정(`strict: true`)합니다.
- 파싱 전 예외 상황(안전 정책 위반 등)을 감지하기 위해 `output[0].content[0].refusal` 값을 최우선으로 검증합니다.

### 1.2 Request Body 형식
```json
{
  "model": "gpt-5.4-mini",
  "input": [
    { "role": "system", "content": "You are a professional animation scene generator." },
    { "role": "user", "content": "해질녘 바닷가에서 두 사람이 대화하는 장면" }
  ],
  "text": {
    "format": {
      "type": "json_schema",
      "name": "scene_generation",
      "strict": true,
      "schema": {
        "type": "object",
        "properties": {
          "scene_title": { "type": "string", "description": "전체 장면을 요약하는 짧은 제목 (한국어)" },
          "setting": { "type": "string", "description": "시간대, 장소, 날씨 등 배경 묘사" },
          "characters": { "type": "string", "description": "등장인물의 외형, 옷차림 등 구체적 묘사" },
          "mood": { "type": "string", "description": "전체적인 조명, 색감, 분위기" },
          "cuts": {
            "type": "array",
            "description": "반드시 기승전결 흐름을 갖는 3개의 컷으로 구성해야 함",
            "minItems": 3,
            "maxItems": 3,
            "items": {
              "type": "object",
              "properties": {
                "cut_order": { "type": "integer", "description": "순서 (1, 2, 3)" },
                "image_generation_prompt": { "type": "string", "description": "정지된 이미지를 생성하기 위한 구체적인 묘사 (반드시 영어로 작성)" },
                "video_generation_prompt": { "type": "string", "description": "이미지가 어떻게 움직여야 하는지에 대한 동적 묘사 (반드시 영어로 작성)" },
                "duration_sec": { "type": "integer", "description": "해당 컷의 지속 시간 (기본 10초 강제)" }
              },
              "required": ["cut_order", "image_generation_prompt", "video_generation_prompt", "duration_sec"],
              "additionalProperties": false
            }
          }
        },
        "required": ["scene_title", "setting", "characters", "mood", "cuts"],
        "additionalProperties": false
      }
    }
  }
}
```

### 1.3 응답(Response) 및 예외 처리
- **정상 처리 (HTTP 200, status: "completed")**: `output[0].content[0].text`의 JSON 문자열을
  파싱하여 비즈니스 객체로 역직렬화합니다.
- **정책 위반 거절 (HTTP 200, type: "refusal")**: `output[0].content[0].refusal`이 null이
  아니거나 타입이 "refusal"인 경우, 프롬프트가 윤리 정책을 위반한 것으로 간주하여
  BusinessException을 발생시킵니다.
- **불완전 응답 (HTTP 200, status: "incomplete")**: max_output_tokens 초과나 내부 중단으로
  응답이 잘린 경우입니다. `incomplete_details.reason` 필드를 통해 원인을 확인할 수 있으며
  (예: "max_output_tokens", "content_filter"), 재시도 없이 BusinessException으로 처리합니다.
- **Rate Limit (HTTP 429)**: 요청 빈도 초과 또는 월 쿼터 소진 시 발생합니다.
  Exponential Backoff로 최대 3회 재시도하며, 재시도 후에도 실패 시 중단합니다.
- **서버 오류 (HTTP 500 / 503)**: OpenAI 서버 측 일시적 오류입니다.
  Exponential Backoff로 최대 3회 재시도합니다.
- **클라이언트 오류 (HTTP 400 / 401 / 403)**: 요청 규격이 잘못되었거나 API Key가
  유효하지 않은 경우 발생하며, 로깅 후 재시도 없이 중단합니다 (Fail-fast).

---

## 2. KIE.ai API (Image/Video Generation)
OpenAI가 기획한 컷(Cut) 정보를 바탕으로 실제 미디어 에셋을 생성합니다.
- **Base URL**: `https://api.kie.ai`
- **인증**: `Authorization: Bearer YOUR_API_KEY` (환경변수 `KIE_API_KEY` 사용)
- **통신 패턴**: 비동기 웹훅 파이프라인 (요청 시 Task ID 발급 -> Callback으로 결과 수신 -> 상태 DB 동기화)

### 2.1 Nano Banana (Text to Image)
- **Endpoint**: `POST /api/v1/jobs/createTask`
- **Model**: `google/nano-banana`
- **Request Body**:
```json
{
  "model": "google/nano-banana",
  "callBackUrl": "https://your-domain.com/api/callback",
  "input": {
    "prompt": "이미지 설명 텍스트",
    "output_format": "png",
    "image_size": "1:1"
  }
}
```

### 2.2 Kling 2.6 (Text/Image to Video)
- **Endpoint**: `POST /api/v1/jobs/createTask`
- **Model**: `kling-2.6/text-to-video` 또는 `kling-2.6/image-to-video`
- **Request Body (Image to Video 예시)**:
```json
{
  "model": "kling-2.6/image-to-video",
  "callBackUrl": "https://your-domain.com/api/callback",
  "input": {
    "prompt": "영상 설명 텍스트",
    "image_urls": ["https://example.com/your-image.png"],
    "sound": false,
    "duration": "5"
  }
}
```

### 2.3 Task 상태 조회 (Polling) - 선택 사항
웹훅(Callback) 수신이 불가능한 로컬 개발 환경이나, 이벤트 유실 시의 Self-healing을 위해 활용됩니다.
- **Endpoint**: `GET /api/v1/jobs/recordInfo?taskId={taskId}`
- **Response (성공 시)**:
```json
{
  "code": 200,
  "data": {
    "taskId": "task_12345678",
    "model": "kling-2.6/text-to-video",
    "state": "success",
    "resultJson": "{\"resultUrls\":[\"https://example.com/generated-video.mp4\"]}"
  }
}
```

### 2.4 KIE API 공식 Task 상태 (State) 목록 및 예외 처리 정책
공식 문서에 명시된 Task 상태값은 총 5가지이며, 백엔드 파이프라인의 폴링 및 웹훅 처리는 이 기준을 엄격하게 따릅니다.

- `waiting`: 큐에 대기 중 (중간 상태, 무시)
- `queuing`: 처리 큐 진입 (중간 상태, 무시)
- `generating`: 현재 생성 중 (중간 상태, 무시)
- **`success`**: 완료 (성공 - 다음 파이프라인 진행)
- **`fail`**: 실패 (종료 - 파이프라인 FAILED 마킹)

**[예외 처리 설계]**
- `"failed"`, `"error"`, `"canceled"` 등의 값은 공식 상태값이 아니므로 제외합니다.
- `fail` 상태 수신 시 `failCode`와 `failMsg` 필드를 통해 실패 원인을 파악할 수 있으며, 시스템 로깅 후 사용자에게 안내합니다.
- 웹훅이나 폴링으로 `success`나 `fail`이 아닌 중간 상태(예: `generating`)가 들어올 경우, 상태를 섣불리 실패로 처리하지 않고 조용히 무시(Ignore)하도록 방어 로직이 구현되어 있습니다.
