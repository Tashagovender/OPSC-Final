package com.example.versiononetasktwo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class Camera : AppCompatActivity() {

    lateinit var imgCam: ImageView
    lateinit var btnCapture: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        //typecast
        imgCam=findViewById(R.id.imgViewcam)
        btnCapture=findViewById(R.id.btnCapture)
        btnCapture.setOnClickListener{
            openCamera()}
    }
    fun openCamera()
    {
        val cameraIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE)
    }
    override  fun onActivityResult(requestCode:Int,resultCode: Int,data:Intent?)
    {
       super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== CAMERA_REQUEST_CODE &&
                resultCode== Activity.RESULT_OK)
        {
            //bitmap --compressed base64
            val imageBitmap=data?.extras?.get("data") as Bitmap
            imgCam.setImageBitmap(imageBitmap)
            //now save to firebase
            saveImageToFirebase(imageBitmap)

        }

    }
    companion object{
        const val CAMERA_REQUEST_CODE=100;
    }
    fun saveImageToFirebase(imageBitmap:Bitmap)
    {
        val outputStream=ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        val base64Image= Base64.encodeToString(outputStream.toByteArray(),
            Base64.DEFAULT)
        val databaseReference=FirebaseDatabase.getInstance().getReference("images")
        val imageId=databaseReference.push().key
        databaseReference.child(imageId!!).setValue(base64Image)

    }

}