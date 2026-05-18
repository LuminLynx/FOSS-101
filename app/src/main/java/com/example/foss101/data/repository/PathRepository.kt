package com.example.foss101.data.repository

import com.example.foss101.data.remote.api.PathApiService
import com.example.foss101.model.CompletionRecord
import com.example.foss101.model.GradeResult
import com.example.foss101.model.Path
import com.example.foss101.model.ReviewDue
import com.example.foss101.model.UnitDetail

interface PathRepository {
    suspend fun getPath(pathId: String): Path
    suspend fun getUnit(unitId: String): UnitDetail
    suspend fun markComplete(unitId: String): CompletionRecord

    /**
     * F4 — submit the user's open-ended decision-prompt answer for grading.
     * The server records a completion + persists per-criterion grades on
     * success; the local completion cache is updated so path home reflects
     * the new state without an extra round trip.
     */
    suspend fun submitGrade(unitId: String, answer: String): GradeResult

    /**
     * Pull the authenticated user's completion list from the server and
     * replace the local cache with it. Used to seed the cache after sign-in
     * or on a fresh install so completion state syncs across devices for
     * the same account.
     */
    suspend fun syncCompletedUnits()

    /**
     * F5 / D5 — spaced reviews currently due for the user. Surfaced
     * alongside (never gating) the next new unit on path home; the
     * ViewModel calls this best-effort and a failure must not block
     * the rest of the screen.
     */
    suspend fun listDueReviews(): List<ReviewDue>

    /**
     * F5 / D6 — mark a due review done (advances the spaced-review
     * ladder server-side). Best-effort: the server gates it
     * (404 never-completed / 409 not-yet-due) and the caller
     * swallows failures.
     */
    suspend fun markReviewed(unitId: String)
}

class ApiPathRepository(
    private val pathApiService: PathApiService,
    private val completionCache: CompletionCache
) : PathRepository {

    override suspend fun getPath(pathId: String): Path = pathApiService.getPath(pathId)

    override suspend fun getUnit(unitId: String): UnitDetail = pathApiService.getUnit(unitId)

    override suspend fun markComplete(unitId: String): CompletionRecord {
        val record = pathApiService.postCompletion(unitId)
        completionCache.add(record.unitId)
        return record
    }

    override suspend fun submitGrade(unitId: String, answer: String): GradeResult {
        val result = pathApiService.submitGrade(unitId, answer)
        completionCache.add(result.completion.unitId)
        return result
    }

    override suspend fun syncCompletedUnits() {
        val records = pathApiService.listCompletions()
        completionCache.replaceAll(records.map { it.unitId }.toSet())
    }

    override suspend fun listDueReviews(): List<ReviewDue> =
        pathApiService.listDueReviews()

    override suspend fun markReviewed(unitId: String) =
        pathApiService.markReviewed(unitId)
}
