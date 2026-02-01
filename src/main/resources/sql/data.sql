-- 모든 테이블 데이터 삭제 (외래 키 제약 조건 고려)
TRUNCATE TABLE
    public.tag,
    public.asset,
    public.asset_tag,
    public.scenario,
    public.location,
    public.suspect,
    public.clue,
    public.chat_message_embedding,
    public.game_session,
    public.fact,
    public.scenario_context,
    public.map
RESTART IDENTITY CASCADE;

-- 테스트 데이터 삽입

-- 1. 시나리오 (scenario)
INSERT INTO public.scenario (scenario_id, difficulty, theme, tone, language, incident_type, incident_summary, incident_time_start, incident_time_end, primary_object, crime_time_start, crime_time_end, crime_method, no_supernatural, no_time_travel, created_at)
VALUES (1, 'easy', '저택 살인 사건', '진지한', 'ko', '살인', '깊은 밤, 외딴 저택에서 저명한 사업가가 숨진 채 발견되었습니다. 범인은 내부에 있습니다.', '22:00:00', '23:00:00', '피해자의 일기장', '22:30:00', '22:45:00', '등 뒤에서 날카로운 흉기로 찔림', true, true, now());

-- 2. 장소 (location)
INSERT INTO public.location (scenario_id, location_id, name, can_see, cannot_see, access_requires)
VALUES
    (1, 1, '로비', '[]', '[]', null),
    (1, 2, '도서관', '[]', '[]', '{"item": "도서관 열쇠"}'),
    (1, 3, '주방', '[]', '[]', null);

-- 3. 용의자 (suspect)
INSERT INTO public.suspect (scenario_id, suspect_id, name, role, age, gender, description, is_culprit, motive, alibi_summary, speech_style, emotional_tendency, lying_pattern, critical_clue_ids, x, y)
VALUES
    (1, 101, '집사 지브스', '집사', 58, '남성', '수십 년간 이 저택에서 일해 온 충직한 집사. 항상 침착함을 유지합니다.', false, null, '사건 추정 시각에 자신의 방에서 휴식을 취하고 있었다고 주장합니다.', '정중하고 격식 있는', '차분함', '눈을 마주치지 못함', '[]', 5, 15),
    (1, 102, '빅토리아 부인', '피해자의 아내', 45, '여성', '우아하고 기품 있는 여성이지만, 최근 남편과의 불화가 잦았다는 소문이 있습니다.', true, '남편의 막대한 유산을 상속받기 위해', '사건 추정 시각에 정원에서 산책을 하고 있었다고 주장합니다.', '고상하고 약간은 거만한', '불안정', '말을 더듬음', '[2, 3]', 15, 10),
    (1, 103, '요리사 앙투안', '요리사', 35, '남성', '다혈질적인 성격의 프랑스인 요리사. 최근 피해자와 크게 다툰 적이 있습니다.', false, null, '사건 추정 시각에 주방에서 다음 날 아침 식사를 준비하고 있었다고 주장합니다.', '거칠고 직설적인', '다혈질', '목소리가 커짐', '[]', 25, 5);

-- 4. 단서 (clue)
INSERT INTO public.clue (scenario_id, clue_id, name, location_id, description, logic_explanation, decoded_answer, is_red_herring, related_fact_ids, x, y)
VALUES
    (1, 1, '피 묻은 칼', 3, '주방 싱크대에서 발견된 피 묻은 식칼. 혈액형은 피해자의 것과 일치합니다.', '범행에 사용된 흉기일 가능성이 높습니다.', null, false, '[]', 26, 6),
    (1, 2, '찢어진 드레스 조각', 2, '도서관 책상 밑에서 발견된 값비싼 드레스의 찢어진 조각. 빅토리아 부인의 드레스와 일치합니다.', '몸싸움 중에 찢어진 것으로 보이며, 빅토리아 부인이 현장에 있었음을 시사합니다.', null, false, '[102]', 12, 12),
    (1, 3, '수수께끼의 쪽지', 1, '로비 테이블 위에 놓여 있던 쪽지. "모든 것을 끝내야겠어."라고 적혀 있습니다.', '피해자의 자살을 위장하기 위해 범인이 남긴 것일 수 있습니다.', null, true, '[]', 8, 8);

-- 5. 사실 (fact)
INSERT INTO public.fact (scenario_id, suspect_id, fact_id, threshold, type, content, embedding)
VALUES
    (1, 102, 102, 5, 'secret', '{"secret": "빅토리아 부인은 최근 거액의 도박 빚을 졌으며, 남편에게 유산을 빨리 상속받고 싶어 했습니다."}', null);

-- 6. 맵 (map)
INSERT INTO public.map (scenario_id, map_id, type, name, x, y, width, height, extra_data)
VALUES
    (1, 1, 'room', '로비', 2, 2, 10, 15, '{}'),
    (1, 2, 'room', '도서관', 15, 2, 8, 10, '{}'),
    (1, 3, 'room', '주방', 25, 2, 10, 8, '{}'),
    (1, 4, 'corridor', '복도', 12, 5, 3, 5, '{}');

-- 7. 시나리오 컨텍스트 (scenario_context)
INSERT INTO public.scenario_context (scenario_id, context_id, type, content, extra_data, embedding)
VALUES
    (1, 1, 'incident', '사건의 배경: 피해자는 큰 성공을 거둔 사업가였지만, 주변에 적이 많았습니다. 특히 그의 막대한 재산을 노리는 사람들이 많았습니다.', '{}', null),
    (1, 2, 'world', '시간적 배경은 1920년대 영국입니다. 귀족 문화가 남아있으며, 격식과 명예를 중요시합니다.', '{}', null);
