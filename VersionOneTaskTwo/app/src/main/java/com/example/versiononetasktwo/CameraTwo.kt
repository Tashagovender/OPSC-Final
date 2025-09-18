package com.example.versiononetasktwo

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class CameraTwo : AppCompatActivity() {
    //variables
    lateinit var imgViewTwo: ImageView
    lateinit var btnChoose: Button
    lateinit var btnTakePic: Button
    lateinit var btnUpload: Button

    //globals
    var filePath: Uri? = null
    var PICK_IMAGE_REQUEST = 22
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
    val firestore = FirebaseFirestore.getInstance()
    val REQUEST_IMAGE_CAPTURE = 1
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_two)
        //typecasts
        imgViewTwo = findViewById(R.id.imageViewTwo)
        btnChoose = findViewById(R.id.btnCamTwoChoose)
        btnTakePic = findViewById(R.id.btnCamTwoTakePic)
        btnUpload = findViewById(R.id.btnCamTwoUpload)

        //choose image btn
        btnChoose.setOnClickListener {
            selectImage()
        }
        //save to image view
        btnTakePic.setOnClickListener {
            dispatchTakePictureIntent()
        }
        //
        btnUpload.setOnClickListener {
            uploadImage()
        }

    } //on create ends

    //select image method
    fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent, "Select image"),
            PICK_IMAGE_REQUEST
        )
    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode ==
            Activity.RESULT_OK
        ) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgViewTwo.setImageBitmap(imageBitmap)
            //save to firestore
            saveImageToFirebase(imageBitmap)

        }
    }//method ends

    fun uploadImage() {
        filePath?.let { filePath ->
            if (contentResolver.openInputStream(filePath) != null) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading")
                progressDialog.show()
                val ref = storageReference.child("images/${UUID.randomUUID()}.jpg")
                ref.putFile(filePath)
                    .addOnSuccessListener { taskSnapshot ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            val imageURL = uri.toString()
                            saveImageUrlToFireStore(imageURL)
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to Convert", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to Upload", Toast.LENGTH_SHORT).show()
                    }.addOnProgressListener { taskSnapshot ->
                        val progress =
                            (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                        progressDialog.setMessage("Uploaded: ${progress.toInt()}%")
                    }
            } else {
                Toast.makeText(this, "Image doesn't exist at location", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToFireStore(imageURL: String) {
        val imageMap = hashMapOf(
            "imageUrl" to imageURL
        )
        firestore.collection("images")
            .add(imageMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved to database", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show()
            }
    }//method ends

    fun saveImageToFirebase(imageBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        //image name/image ref
        val imageName = UUID.randomUUID().toString() + ".jpg"
        val imagesRef = storageReference.child("images/$imageName")
        //finally pass through
        imagesRef.putBytes(data)

            .addOnSuccessListener {
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageURL = uri.toString()
                    saveImageUrlToFireStore(imageURL)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }
}




