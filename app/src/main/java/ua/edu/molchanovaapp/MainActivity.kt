package ua.edu.molchanovaapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 1000
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnTakeSelfie = findViewById<Button>(R.id.btnTakeSelfie)
        val btnSendEmail = findViewById<Button>(R.id.btnSendEmail)
        btnTakeSelfie.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        btnSendEmail.setOnClickListener{
            sendEmail()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageView: ImageView = findViewById(R.id.imageView)
            val bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
//            savePhotoTemporarily(bitmap)
            try {
                val photoFile = createImageFile()
                FileOutputStream(photoFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                }
                if (photoFile.exists() && photoFile.length() > 0) {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider",
                        photoFile
                    )
                } else {
                    Toast.makeText(this, "Помилка при збереженні зображення", Toast.LENGTH_SHORT).show()
                    return
                }            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "SELFIE_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun sendEmail() {
        if (photoUri == null) {
            Toast.makeText(this, "Будь ласка зробіть спочатку фото", Toast.LENGTH_SHORT).show()
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "DigiJED Марія Молчанова")
            putExtra(Intent.EXTRA_TEXT, """
                |Шановні пані та панове,
                |
                |Надсилаю вам селфі для завдання.
                |
                |Посилання на репозиторій: """.trimMargin())
            putExtra(Intent.EXTRA_STREAM, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(emailIntent, "Виберіть клієнт для відправки імейлу."))

        try {
            startActivity(Intent.createChooser(emailIntent, "Імейл відправлено."))
        } catch (e: Exception) {
            Toast.makeText(this, "Не знайдено програми для відправки імейлу.", Toast.LENGTH_SHORT).show()
        }

    }
}