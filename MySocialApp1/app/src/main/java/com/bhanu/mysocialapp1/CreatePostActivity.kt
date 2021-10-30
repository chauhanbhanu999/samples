package com.bhanu.mysocialapp1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.bhanu.mysocialapp1.daos.PostDao

class CreatePostActivity : AppCompatActivity() {

    private lateinit var postBtn: Button
    private lateinit var postInput: EditText
    private lateinit var postDao: PostDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postBtn = findViewById(R.id.postBtn)
        postInput = findViewById(R.id.postInput)
        postDao = PostDao()
        postBtn.setOnClickListener {
            val input = postInput.text.toString().trim()
            if (input.isNotEmpty()) {
                postDao.addPost(input)
            }
        }
    }
}