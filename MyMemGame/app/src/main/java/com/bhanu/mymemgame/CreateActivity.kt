package com.bhanu.mymemgame

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bhanu.mymemgame.models.BoardSize
import com.bhanu.mymemgame.utils.BitmapScaler
import com.bhanu.mymemgame.utils.EXTRA_BOARD_SIZE
import com.bhanu.mymemgame.utils.EXTRA_GAMENAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class CreateActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "CreateActivity"
        private const val MAX_GAMELENGTH = 14
        private const val MIN_GAMELENGTH = 3
    }
    private  lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private var numimagesRequired =-1
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var pbUploading: ProgressBar
    private lateinit var rvImagePicker: RecyclerView
    private lateinit var clCreateActivity: ConstraintLayout
    private  var uriCount: Int = 0
    private val chosenImageUris= mutableListOf<Uri>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        btnSave = findViewById(R.id.btnSave)
        pbUploading = findViewById(R.id.pbUploading)
        etGameName = findViewById(R.id.etGameName)
        rvImagePicker = findViewById(R.id.rvImagePicker)
        clCreateActivity = findViewById(R.id.clCreateActivity)
        val imagePickerContract = registerForActivityResult(ActivityResultContracts.GetMultipleContents())   { result ->
            Log.i("helpme",result.toString())
            Log.i("helpme","${result.size}")
            if (result == null) {
                Log.w(TAG, "no data")
            }   else    {
                    result.forEach { uri ->
                    chosenImageUris.add(uri)
                    adapter.notifyDataSetChanged()
                    uriCount += 1
                }
            }
            supportActionBar?.title = "choose pics (${uriCount}/ $numimagesRequired)"
            btnSave.isEnabled = shouldEnableSaveButton()
        }
        val reqPermissionCont = registerForActivityResult(ActivityResultContracts.RequestPermission())  { granted: Boolean ->
            if(granted) {
                Log.i("permission:","granted")
            }   else    {
                Log.i("permission:","denied")
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numimagesRequired=boardSize.getNumPairs()
        supportActionBar?.title = "choose pics (0 / $numimagesRequired)"

        btnSave.setOnClickListener {
            saveDataToFirebase()
        }

        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAMELENGTH))
        etGameName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }
        })

        adapter = ImagePickerAdapter(this,chosenImageUris,boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceholderClicked() {
                when{
                    ContextCompat.checkSelfPermission(
                        this@CreateActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )==PackageManager.PERMISSION_GRANTED -> {
                        //got permission so call image picker contract and launch it
                        imagePickerContract.launch("image/*")
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this@CreateActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {  Snackbar.make(clCreateActivity,
                                R.string.permission_required,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction("again") {
                            reqPermissionCont.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                        //additional rationale to be displayed
                    }
                    else ->{
                        reqPermissionCont.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        //permission not asked
                    }
                }
            }
        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this,boardSize.getWidth())

    }

    private fun saveDataToFirebase() {
        val customGameName: String = etGameName.text.toString()
        //downscaling the img to reduce the size of game when saved on cloud/firebase
        Log.i(TAG," save data to firebase")
        btnSave.isEnabled = false
        //check we do not over write someones data
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            if (document!=null && document.data != null)    {
                AlertDialog.Builder(this)
                    .setTitle("name taken")
                    .setMessage("$customGameName already exists. choose new name!!")
                    .setPositiveButton("ok",null)
                    .show()
                btnSave.isEnabled = true
            } else {
                handleImageUploading(customGameName)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG,"encountered error",exception)
            Snackbar.make(clCreateActivity,"encountered error",Snackbar.LENGTH_SHORT).show()
            //Toast.makeText(this,"encountered err",Toast.LENGTH_SHORT).show()
            btnSave.isEnabled = true
        }
    }

    private fun handleImageUploading(gameName: String) {
        pbUploading.visibility = View.VISIBLE
        var didEncounterError = false
        val uploadedImageUrls: MutableList<String> = mutableListOf<String>()
        for((index: Int, photoUri: Uri) in chosenImageUris.withIndex())  {
            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoreference: StorageReference = storage.reference.child(filePath)
            photoreference.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG,"upload bytes ${photoUploadTask.result?.bytesTransferred}")
                    photoreference.downloadUrl
                }.addOnCompleteListener { downloadUrlTask ->
                    if (!downloadUrlTask.isSuccessful)  {
                        Log.e(TAG,"exception with firebase",downloadUrlTask.exception)
                        Toast.makeText(this,"upload img failed",Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }
                    if(didEncounterError)   {
                        pbUploading.visibility = View.GONE
                        return@addOnCompleteListener
                    }
                    val downloadUrl: String = downloadUrlTask.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    pbUploading.progress = uploadedImageUrls.size * 100 / chosenImageUris.size
                    Log.i(TAG,"finished uploading$photoUri,num=${uploadedImageUrls.size}")
                    if (uploadedImageUrls.size == chosenImageUris.size) {
                        handleAllImagesUploaded(gameName,uploadedImageUrls)
                    }
                }
        }

    }

    private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {
        db.collection("games").document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
                pbUploading.visibility = View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG,"exception game creationn",gameCreationTask.exception)
                    Toast.makeText(this,"game create failed",Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.i(TAG,"created $gameName success")
                AlertDialog.Builder(this)
                    .setTitle("Uploaded. Play $gameName")
                    .setPositiveButton("ok")    {_,_ ->
                        val resultData = Intent().apply {
                            putExtra(EXTRA_GAMENAME,gameName)
                        }
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }.show()
            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {

        val original_bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)   {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        }   else    {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG,"the og --> ${original_bitmap.width}  ${original_bitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(original_bitmap, 250)
        Log.i(TAG,"scaled --> ${scaledBitmap.width}  ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG,60,byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    private fun shouldEnableSaveButton(): Boolean {
        if (chosenImageUris.size != numimagesRequired)  {
            return false
        }
        if (etGameName.text.isBlank() || etGameName.text.length < MIN_GAMELENGTH)   {
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)   {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }



}