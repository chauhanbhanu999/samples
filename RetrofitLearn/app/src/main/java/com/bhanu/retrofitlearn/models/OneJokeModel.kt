package com.bhanu.retrofitlearn.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OneJokeModel(
    @Json(name = "joke")
    val joke: String
)