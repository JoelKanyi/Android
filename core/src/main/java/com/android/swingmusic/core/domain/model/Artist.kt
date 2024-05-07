package com.android.swingmusic.core.domain.model

data class Artist(
    val artisthash: String,
    val colors: List<String>,
    val createdDate: Double,
    val helpText: String,
    val image: String,
    val name: String
)
