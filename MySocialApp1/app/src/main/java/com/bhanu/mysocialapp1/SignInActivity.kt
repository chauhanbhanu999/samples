package com.bhanu.mysocialapp1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ProgressBar
import com.bhanu.mysocialapp1.daos.UserDao
import com.bhanu.mysocialapp1.models.User


class SignInActivity : AppCompatActivity() {

    companion object    {
        private const val TAG = "signinActivity"
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var signInBtn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar
    private val mainActLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {res ->
        //check if we got data or not
        //if (res.resultCode)    {
            val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
            handleSignInResult(task)
        //}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        signInBtn = findViewById(R.id.signInBtn)
        progressBar = findViewById(R.id.progressBar)

        signInBtn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
            super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        updateUI(currentUser!!)
    }

    private fun signIn() {
        var signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
        //give data

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>?) {
        try {
            val account = completedTask?.getResult(ApiException::class.java)
            Log.d(TAG,"firebaseAuthGoogle"+account?.id)
            firebaseAuthWithGoogle(account?.idToken!!)
        }   catch (e: ApiException) {
            Log.w(TAG,"signinerror"+e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        signInBtn.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO)  {
            val auth = auth.signInWithCredential(credential)
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            withContext(Dispatchers.Main)   {
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null)    {

            val user = User(firebaseUser.uid,firebaseUser.displayName,firebaseUser.photoUrl.toString())
            val usersDao = UserDao()
            usersDao.addUser(user)

            val mainActIntent = Intent(this,MainActivity::class.java)
            Log.i(TAG,"bula lo bey")
            mainActLauncher.launch(mainActIntent)
            Log.i(TAG,"abe ab toh aajaun naaa?")
            //finish()
        }   else    {
            signInBtn.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

}

//https://firebase.google.com/docs/auth/android/google-signin?authuser=2
//https://youtu.be/peobCNlQZY8?list=PLUcsbZa0qzu3Mri2tL1FzZy-5SX75UJfb&t=905
//auth using firebase UI below
//https://firebase.google.com/docs/auth/android/firebaseui