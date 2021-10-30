package com.bhanu.retrofitlearn.services

import com.bhanu.retrofitlearn.models.OneJokeModel
import retrofit2.Call
import retrofit2.http.GET

interface OneJokeAPI {
    /*https://geek-jokes.sameerkumar.website/api?format=json*/
    @GET("api?format=json")
    fun getOneJoke():Call<OneJokeModel>
}