package com.example.foss101.data.remote.api

import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.GradeResult
import com.example.foss101.model.Path
import com.example.foss101.model.ReviewDue
import com.example.foss101.model.UnitDetail

interface PathApiService {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun postCompletion(unitId: String): CompletionRecord
    /** Returns every completion for the authenticated user, newest first. */
    suspend fun listCompletions(): List<CompletionRecord>
    /**
     * F4 — submit the user's open-ended decision-prompt answer for grading.
     * Server records a completion + grades on success. See
     * backend/app/main.py POST /api/v1/units/{unit_id}/grade.
     */
    suspend fun submitGrade(unitId: String, answer: String): GradeResult

    /**
     * F5 / D5 — spaced reviews due for the authenticated user,
     * ordered by due_at then unit position. GET
     * /api/v1/review-schedule (server NOW() when no due_before).
     */
    suspend fun listDueReviews(): List<ReviewDue>

    /**
     * F5 / D6 — mark a due review done; advances the ladder.
     * POST /api/v1/review-schedule/{unitId}/reviewed. The server
     * gates this (404 if never completed, 409 if not yet due);
     * callers treat it as best-effort.
     */
    suspend fun markReviewed(unitId: String)
}

class PathApiException(
    override val message: String,
    val code: String? = null,
    val statusCode: Int? = null
) : RuntimeException(message)
