package com.bhanu.retrofitlearn.services

import com.bhanu.retrofitlearn.models.DogModel
import retrofit2.Call
import retrofit2.http.GET

interface DogAPI {

    @GET("breeds/image/random")
    fun getDogs(): Call<DogModel>
}