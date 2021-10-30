package com.bhanu.mymemgame

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bhanu.mymemgame.models.BoardSize
import com.bhanu.mymemgame.models.MemoryGame
import com.bhanu.mymemgame.models.UserImageList
import com.bhanu.mymemgame.utils.EXTRA_BOARD_SIZE
import com.bhanu.mymemgame.utils.EXTRA_GAMENAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "mainAct"
    }

    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null
    private lateinit var memoryGame: MemoryGame
    private lateinit var clRoot: CoordinatorLayout
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private var boardSize: BoardSize = BoardSize.EASY
    val boardContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result?.resultCode == Activity.RESULT_OK)    {
            val customGameName: String? = result.data?.getStringExtra(EXTRA_GAMENAME)
            if (customGameName == null) {
                Log.e(TAG,"got null game name from createactivity")
                return@registerForActivityResult
            }
            downloadGame(customGameName)
        }
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener {  document ->
            val userImageList: UserImageList? = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null)  {
                Log.e(TAG,"invalid custom game data")
                Snackbar.make(clRoot, "game not find game-> $gameName",Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            for (imageUrl in userImageList.images)  {
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot,"starting $customGameName",Snackbar.LENGTH_LONG).show()
            gameName = customGameName
            setupBoard()
        }.addOnFailureListener { exception ->
            Log.e(TAG,"exception occured",exception)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard=findViewById(R.id.rvBoard)
        clRoot=findViewById(R.id.clRoot)
        tvNumMoves=findViewById(R.id.tvNumMoves)
        tvNumPairs=findViewById(R.id.tvNumPairs)

        setupBoard()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)  {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame())  {
                    showAlertDialog("quit current game?",null,View.OnClickListener {
                        setupBoard()
                    })
                }   else    {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom  ->  {
                showCreationDialog()
                return true
            }
            R.id.mi_download -> {
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog("Fetch mem game", boardDownloadView, View.OnClickListener {
            //grab game name from user
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("create your mem board",boardSizeView,View.OnClickListener {
            //new board size val
            val desiredBoardSize = when(radioGroupSize.checkedRadioButtonId)   {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            //go to new activity to choose photos they like
            val intent = Intent(this,CreateActivity::class.java).apply {
                putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            }
            boardContract.launch(intent)
            Log.i(TAG,"activity launched")

        })
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize)    {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("choose new size",boardSizeView,View.OnClickListener {
            //new board size val
            boardSize = when(radioGroupSize.checkedRadioButtonId)   {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("cancel",null)
            .setPositiveButton("ok",)   {_, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setupBoard() {
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when (boardSize)    {
            BoardSize.EASY -> {
                tvNumMoves.text="Easy: 4 x 2"
                tvNumPairs.text="Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text="Medium: 6 x 3"
                tvNumPairs.text="Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text="Hard: 6 x 4"
                tvNumPairs.text="Pairs: 0 / 12"
            }
        }
        tvNumPairs.setTextColor((ContextCompat.getColor(this,R.color.color_progress_none)))
        memoryGame = MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(this,boardSize, memoryGame.cards,object: MemoryBoardAdapter.CardClickListener {
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter=adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager=GridLayoutManager(this,boardSize.getWidth())

    }

    private fun updateGameWithFlip(position: Int) {
        //error handling
        if (memoryGame.haveWonGame())   {
            //alert user of invalid move
            Snackbar.make(clRoot,"You have won",Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position))   {
            //alert for invalid move
            Snackbar.make(clRoot,"INVALID MOVE",Snackbar.LENGTH_SHORT).show()
            return
        }
        //flipping the card
        if (memoryGame.flipCard(position))  {
            Log.i(TAG,"match found, pairs ${memoryGame.numPairsFound}")
            //change color
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/ boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full),
            ) as Int
            tvNumPairs.setTextColor(color)
            //increment pairs
            tvNumPairs.text="Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame())   {
                Snackbar.make(clRoot,"CONGRATS! WINNER",Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW,Color.GREEN,Color.MAGENTA)).oneShot()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}