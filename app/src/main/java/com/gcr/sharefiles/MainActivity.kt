package com.gcr.sharefiles

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.gcr.sharefiles.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    var imageCaptured = false
    var status =  false
    lateinit var currentPhotoPath: String
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        requestPermissions()

        binding.btnTakePhoto.setOnClickListener {
            if(PackageManager.PERMISSION_GRANTED == 0)
                dispatchTakePictureIntent()
            else
                Toast.makeText(this,"Denegado",Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveAndShare.setOnClickListener {
            if (imageCaptured){
                createImageFile()
                reset()
            }else{
                Toast.makeText(this,"No image",Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageCaptured = true
            binding.imageView.setImageBitmap(imageBitmap)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
        Toast.makeText(this,"image saved success",Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(context: Context){
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Log.d("TAG","No hay permisos ... solicitalos")
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
        }else{
            Log.d("TAG","Ya tiene hay permisos ...")
        }
    }

    private fun requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                    Toast.makeText(this,"Ya había Otorgado",Toast.LENGTH_SHORT).show()
                    //dispatchTakePictureIntent()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    /**
     * Revisar la documentación para solicitud de permisos
     * https://developer.android.com/training/permissions/requesting#manage-request-code-yourself
     * https://www.youtube.com/watch?v=MVhjLo8bDac
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        isGranted: Boolean ->
        if(isGranted){
            // Permission is granted. Continue the action or workflow in your
            // app.
            Toast.makeText(this,"Otorgado",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"Denegado",Toast.LENGTH_SHORT).show()
        }
    }

    private fun reset(){
        imageCaptured = false
        binding.imageView.setImageBitmap(null)
    }
}