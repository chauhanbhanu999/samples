package com.bhanu.mysocialapp1.daos

import android.util.Log
import com.bhanu.mysocialapp1.models.Post
import com.bhanu.mysocialapp1.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*

class PostDao {
    companion object    {
        private const val TAG = "PostDao"
    }
    val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("posts")
    private val  auth = Firebase.auth

    fun addPost(text: String)   {
        val currentUserId = auth.currentUser!!.uid
        //var user: User?
        GlobalScope.launch{
            val userDao  = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)
            val currentTime = System.currentTimeMillis()
            val post = user?.let { Post(text, it, currentTime) }
            Log.i(TAG, "MIRACLE MIRACLE!!!")
            postCollection.document().set(post!!)
        }
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollection.document(postId).get()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)
            val isLiked = post?.likedBy!!.contains(currentUserId)

            if (isLiked) {
                post?.likedBy!!.remove(currentUserId)
            } else {
                post?.likedBy!!.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }
    }
}
//c