package com.bhanu.retrofitlearn.services

import com.bhanu.retrofitlearn.models.JokesModel
import retrofit2.Call
import retrofit2.http.GET

interface JokesAPI {

    @GET("jokes/random")
    fun getJokes(): Call<JokesModel>
}