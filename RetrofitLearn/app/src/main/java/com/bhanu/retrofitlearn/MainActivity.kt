package com.bhanu.retrofitlearn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bhanu.retrofitlearn.databinding.ActivityMainBinding
import com.bhanu.retrofitlearn.models.DogModel
import com.bhanu.retrofitlearn.models.JokesModel
import com.bhanu.retrofitlearn.models.OneJokeModel
import com.bhanu.retrofitlearn.services.DogAPI
import com.bhanu.retrofitlearn.services.JokesAPI
import com.bhanu.retrofitlearn.services.OneJokeAPI
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "mainAct"
    }
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view  = binding.root
        setContentView(view)


        val jokeRetro = Retrofit.Builder()
            .baseUrl("https://official-joke-api.appspot.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val onejokeRetro = Retrofit.Builder()
            .baseUrl("https://geek-jokes.sameerkumar.website/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        /*https://geek-jokes.sameerkumar.website/api?format=json*/
        /*todo add share button to share dog image*/

        val dogRetro = Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val jokeApi = jokeRetro.create(JokesAPI::class.java)
        val onejokeApi = onejokeRetro.create(OneJokeAPI::class.java)
        val dogApi = dogRetro.create(DogAPI::class.java)


        binding.fetchBtn.setOnClickListener {
            jokeApi.getJokes().enqueue(object: Callback<JokesModel> {
                override fun onResponse(call: Call<JokesModel>, response: Response<JokesModel>) {

                    if (!response.isSuccessful) {
                        Toast.makeText(this@MainActivity,"2jokes no call!!",Toast.LENGTH_SHORT).show()
                        return
                    }

                    val body = response.body()!!
                    val setup = body.setup
                    val punchline = body.punchline

                    binding.setupTV.text = setup
                    binding.punchlineTV.text = punchline
                }

                override fun onFailure(call: Call<JokesModel>, t: Throwable) {
                    Log.i(TAG,"errror joke model")
                }
            })

            onejokeApi.getOneJoke().enqueue(object: Callback<OneJokeModel> {
                override fun onResponse(
                    call: Call<OneJokeModel>,
                    response: Response<OneJokeModel>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@MainActivity,"onejoke notfound!!",Toast.LENGTH_SHORT).show()
                        return
                    }
                    val body = response.body()!!
                    val onejoke = body.joke

                    binding.punchlineTV2.text = onejoke
                }

                override fun onFailure(call: Call<OneJokeModel>, t: Throwable) {
                    Log.i(TAG,"error onejoke model")
                }
            })

            dogApi.getDogs().enqueue(object: Callback<DogModel> {
                override fun onResponse(call: Call<DogModel>, response: Response<DogModel>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@MainActivity,"doggy not found!!",Toast.LENGTH_SHORT).show()
                        return
                    }

                    val body = response.body()!!
                    val imgUrl = body.message
                    Picasso.get()
                        .load(imgUrl)
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .into(binding.dogIV)
                }

                override fun onFailure(call: Call<DogModel>, t: Throwable) {
                    Log.i(TAG,"error dog model")
                }
            })
        }


    }
}