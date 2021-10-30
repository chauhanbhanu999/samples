package com.bhanu.mysocialapp1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bhanu.mysocialapp1.models.Post
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: IPostAdapter) : FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
    options
) {
    private val  auth = Firebase.auth

    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val postText: TextView = itemView.findViewById(R.id.postTitle)
        val userText: TextView = itemView.findViewById(R.id.userName)
        val createdAt: TextView = itemView.findViewById(R.id.createdAt)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val likeBtn: ImageView = itemView.findViewById(R.id.likeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false))
        viewHolder.likeBtn.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.postText.text = model.text
        holder.userText.text =  model.createdBy.displayName
        holder.likeCount.text = model.likedBy.size.toString()
        Glide.with(holder.userImage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.userImage).toString()
        holder.createdAt.text = Utils.getTimeAgo(model.createdAt)

        val currentUserId = auth.currentUser!!.uid
        val isLiked = model.likedBy!!.contains(currentUserId)
        if (isLiked) {
            holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.context,R.drawable.ic_liked))
        } else {
            holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.context,R.drawable.ic_unliked))
        }

    }
}

interface IPostAdapter {
    fun onLikeClicked(postId: String)
}