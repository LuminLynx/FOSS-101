package com.perpenda.data.remote.api

import com.perpenda.data.remote.model.RemoteCategory
import com.perpenda.data.remote.model.RemoteGlossaryTerm

interface GlossaryApiService {
    suspend fun getTerms(): List<RemoteGlossaryTerm>
    suspend fun getTermDetails(termId: String): RemoteGlossaryTerm
    suspend fun getCategories(): List<RemoteCategory>
    suspend fun getTermsByCategory(categoryId: String): List<RemoteGlossaryTerm>
    suspend fun searchTerms(query: String): List<RemoteGlossaryTerm>
}
