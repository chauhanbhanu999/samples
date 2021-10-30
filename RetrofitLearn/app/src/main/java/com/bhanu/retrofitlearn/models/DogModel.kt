package com.bhanu.retrofitlearn.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DogModel(
    @Json(name = "message")
    val message: String,
    @Json(name = "status")
    val status: String
)