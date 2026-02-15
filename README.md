# MaechuriMainServer

## API Documentation

### Interaction API

#### Interact with Scenario Objects

Interact with suspects, clues, or detective in a scenario. Automatically saves interaction records when `gameSessionId` is provided.

**Endpoint:** `POST /api/scenarios/{scenarioId}/interact/{objectId}`

**Path Parameters:**
- `scenarioId` (Long): The ID of the scenario
- `objectId` (String): The object identifier in format `tag:id`
  - `s:id` - Suspect (e.g., `s:101`)
  - `c:id` - Clue (e.g., `c:1`)
  - `i:id` - Detective/Investigator

**Request Body:**
```json
{
  "message": "안녕하세요",
  "gameSessionId": "session-123"
}
```

**Response for Suspect Interaction:**
```json
{
  "type": "two-way",
  "message": "대답 내용...",
  "pressure": 50,
  "pressureDelta": -15,
  "revealedFactIds": [31, 32]
}
```

**Response for Clue Interaction:**
```json
{
  "type": "simple",
  "name": "피 묻은 칼",
  "message": "주방 싱크대에서 발견된 피 묻은 식칼."
}
```

**Automatic Record Saving:**
- When `gameSessionId` is provided in the request:
  - Suspect interactions (`s:n`) are saved to the record table
  - Clue interactions (`c:n`) are saved to the record table
  - Revealed facts from suspect conversations are automatically saved
  
---

### Records API

#### Get All Interacted Records

Retrieve all records that have been interacted with in a game session.

**Endpoint:** `GET /api/scenarios/{scenarioId}/records?gameSessionId={gameSessionId}`

**Path Parameters:**
- `scenarioId` (Long): The ID of the scenario

**Query Parameters:**
- `gameSessionId` (String): The game session identifier

**Response:**
```json
{
  "records": [
    {
      "name": "피 묻은 칼",
      "content": "주방 싱크대에서 발견된 피 묻은 식칼. 혈액형은 피해자의 것과 일치합니다."
    },
    {
      "name": "집사 지브스",
      "content": "수십 년간 이 저택에서 일해 온 충직한 집사. 항상 침착함을 유지합니다."
    }
  ]
}
```

**Example Request:**
```
GET /api/scenarios/1/records?gameSessionId=session-123
```

**Error Responses:**
- `400 Bad Request`: Invalid scenarioId or blank gameSessionId

---

#### Get Record by ID

Retrieve a specific record (fact, suspect, or clue) from a scenario.

**Endpoint:** `GET /api/scenarios/{scenarioId}/records/{recordId}`

**Path Parameters:**
- `scenarioId` (Long): The ID of the scenario
- `recordId` (String): The record identifier in format `tag:id`
  - `f:id` - Fact record (e.g., `f:102`)
  - `s:id` - Suspect record (e.g., `s:101`)
  - `c:id` - Clue record (e.g., `c:1`)

**Response:**
```json
{
  "name": "피 묻은 칼",
  "content": "주방 싱크대에서 발견된 피 묻은 식칼. 혈액형은 피해자의 것과 일치합니다."
}
```

**Example Requests:**

Get a clue:
```
GET /api/scenarios/1/records/c:1
```

Get a suspect:
```
GET /api/scenarios/1/records/s:101
```

Get a fact:
```
GET /api/scenarios/1/records/f:102
```

**Error Responses:**
- `400 Bad Request`: Invalid record ID format or invalid tag
- `404 Not Found`: Record not found

