package com.perpenda.data.remote.model

import com.perpenda.model.Category

data class RemoteCategory(
    val id: String,
    val name: String,
    val description: String
)

fun RemoteCategory.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        description = description
    )
}
