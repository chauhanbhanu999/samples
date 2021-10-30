package com.bhanu.mysocialapp1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bhanu.mysocialapp1.daos.PostDao
import com.bhanu.mysocialapp1.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private var postDao: PostDao = PostDao()
    private lateinit var fActionBtn: FloatingActionButton
    private val createPostActLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fActionBtn = findViewById(R.id.fActionBtn)
        recyclerView = findViewById(R.id.recyclerView)

        fActionBtn.setOnClickListener {
            val createPostActIntent = Intent(this,CreatePostActivity::class.java)
            createPostActLauncher.launch(createPostActIntent)
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        val postsCollections = postDao.postCollection
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }
}