package com.bhanu.mymemgame.models

import com.bhanu.mymemgame.utils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
    private val customImages: List<String>?
) {


    val cards: List<MemCard>
    var numPairsFound = 0
    private var numCardFlips = 0

    private var indexOfSingleSelectedCard: Int? = null

    init{
        if (customImages == null)   {
            val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedImages = (chosenImages + chosenImages).shuffled()
            cards = randomizedImages.map{MemCard(it)}
        }   else    {
            val randomizedImages: List<String> = (customImages + customImages ).shuffled()
            cards = randomizedImages.map { MemCard(it.hashCode(),it) }
        }

    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]
        //3 cases
        //0 flipped -> restore cards [no-op] + flip selected
        //1 -> flip card + check for match
        //2 -> restore cards + flip selected
        var foundMatch = false
        if (indexOfSingleSelectedCard == null)    {
            //0 or 2
            restoreCards()
            indexOfSingleSelectedCard = position
        }   else    {
            //1
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!,position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp=!card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(pos1: Int, pos2: Int): Boolean {
        if (cards[pos1].identifier != cards[pos2].identifier)   {
            return false
        }
        cards[pos1].isMatched = true
        cards[pos2].isMatched = true
        numPairsFound++
        return true

    }

    private fun restoreCards() {
        for (card in cards) {
            if(!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return  cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        //#moves = 1/2 times #cardFlips
        return numCardFlips / 2
    }

}