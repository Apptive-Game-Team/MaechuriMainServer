package com.maechuri.mainserver.game.dto.solve

data class AiSolveResponse(
    val status: String, // enum: correct, partial, incorrect
    val success: Boolean,
    val culprit_score: Float, // 범인 점수 (0 or 100)
    val reasoning_score: Float, // 추리 점수 (0~100)
    val total_score: Float, // 총점 (범인 40% + 추리 60%)
    val culprit_match: Any, // object: 범인 매칭 상세 (Using Any for now, can be a specific data class later if schema provided)
    val similarity_score: Float, // 임베딩 유사도 (0~1)
    val message: String,
    val feedback: String?, // string?: 상세 피드백
    val hints: List<String>? // string[]?: 힌트 (오답/부분정답 시)
) {
    fun toNormalize(): ClientSolveResponse {
        return ClientSolveResponse(
            status,
            success,
            culprit_score,
            reasoning_score,
            total_score,
            culprit_match,
            similarity_score,
            message,
            feedback,
            hints
        )
    }
}

data class ClientSolveResponse(
    val status: String, // enum: correct, partial, incorrect
    val success: Boolean,
    val culpritScore: Float, // 범인 점수 (0 or 100)
    val reasoningScore: Float, // 추리 점수 (0~100)
    val totalScore: Float, // 총점 (범인 40% + 추리 60%)
    val culpritMatch: Any, // object: 범인 매칭 상세 (Using Any for now, can be a specific data class later if schema provided)
    val similarityScore: Float, // 임베딩 유사도 (0~1)
    val message: String,
    val feedback: String?, // string?: 상세 피드백
    val hints: List<String>? // string[]?: 힌트 (오답/부분정답 시)
)