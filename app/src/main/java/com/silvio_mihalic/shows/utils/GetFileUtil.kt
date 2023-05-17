package com.silvio_mihalic.shows.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object GetFileUtil {

    /**
     * Create file object from uri which can be uploaded to server
     */
    fun getFile(mContext: Context?, documentUri: Uri): File {
        val inputStream = mContext?.contentResolver?.openInputStream(documentUri)
        var file = File("")
        inputStream.use { input ->
            file =
                File(mContext?.cacheDir, System.currentTimeMillis().toString() + ".jpg")
            FileOutputStream(file).use { output ->
                val buffer =
                    ByteArray(4 * 1024) // or other buffer size
                var read: Int = -1
                while (input?.read(buffer).also {
                    if (it != null) {
                        read = it
                    }
                } != -1
                ) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file
    }
}
