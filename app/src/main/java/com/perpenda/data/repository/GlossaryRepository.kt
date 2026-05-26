package com.perpenda.data.repository

import com.perpenda.model.Category
import com.perpenda.model.GlossaryTerm

interface GlossaryRepository {
    suspend fun getAllTerms(): List<GlossaryTerm>
    suspend fun getTermById(id: String): GlossaryTerm?
    suspend fun getAllCategories(): List<Category>
    suspend fun searchTerms(query: String): List<GlossaryTerm>
    suspend fun getTermsByCategory(categoryId: String): List<GlossaryTerm>
}
