package com.fgr.dicodingstory.utils

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.fgr.dicodingstory.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun isPasswordInputType(inputType: Int): Boolean {
    return inputType and EditorInfo.TYPE_MASK_CLASS == EditorInfo.TYPE_CLASS_TEXT &&
            inputType and EditorInfo.TYPE_MASK_VARIATION == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
}

fun isEmailInputType(inputType: Int): Boolean {
    return inputType and EditorInfo.TYPE_MASK_CLASS == EditorInfo.TYPE_CLASS_TEXT &&
            inputType and EditorInfo.TYPE_MASK_VARIATION == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
}

fun isEmailValid(email: CharSequence): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun dateToMills(timestamp: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(timestamp) ?: return 0
    val calendar = Calendar.getInstance()
    calendar.time = date

    return date.time
}

private const val MINUTE_MILLIS = 60 * 1000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

fun getTimeAgo(context: Context, mills: Long): String {

    val now = System.currentTimeMillis()
    val diff = now - mills
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

    return when {
        diff < MINUTE_MILLIS -> context.getString(R.string.just_now)
        diff < 2 * MINUTE_MILLIS -> context.getString(R.string.one_minute)
        diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} ${context.getString(R.string.minute)}"
        diff < 90 * MINUTE_MILLIS -> context.getString(R.string.one_hour)
        diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} ${context.getString(R.string.hours)}"
        diff < 48 * HOUR_MILLIS -> context.getString(R.string.yesterday)
        diff < 7 * DAY_MILLIS -> "${diff / DAY_MILLIS} ${context.getString(R.string.days)}"
        else -> return sdf.format(Date(mills))
    }
}

private const val MAXIMAL_SIZE = 1000000

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

private const val FILENAME_FORMAT = "dd-MMM-yyyy"
val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createTempFiles(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateFile(file: File, isBackCamera: Boolean = false) {
    val matrix = Matrix()
    val bitmap = BitmapFactory.decodeFile(file.path)
    val rotation = if (isBackCamera) 90f else -90f
    matrix.postRotate(rotation)
    if (!isBackCamera) {
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    }
    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createTempFiles(context)
    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

fun allPermissionsGranted(baseContext: Context) = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
}