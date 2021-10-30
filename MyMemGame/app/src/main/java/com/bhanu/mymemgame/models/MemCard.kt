package com.bhanu.mymemgame.models

data class MemCard(
        val identifier: Int,
        val imageUrl: String? = null,
        var isFaceUp: Boolean=false,
        var isMatched: Boolean=false
)
