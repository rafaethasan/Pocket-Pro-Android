package com.softx.app.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

internal fun openPdfFile(context: Context, file: File) {
    val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val sharedDir = File(context.cacheDir, "shared-pdf").apply { mkdirs() }
    val sharedFile = File(sharedDir, file.name).apply {
        parentFile?.mkdirs()
        file.copyTo(this, overwrite = true)
    }
    var externalSaved = false
    if (downloadsDir != null) {
        File(downloadsDir, file.name).apply {
            parentFile?.mkdirs()
            file.copyTo(this, overwrite = true)
            externalSaved = true
        }
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        sharedFile,
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.name.removeSuffix(".pdf"))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val shareChooser = Intent.createChooser(shareIntent, "Share PDF").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareChooser)
        Toast.makeText(
            context,
            if (externalSaved) "PDF saved and ready to share." else "PDF ready to share.",
            Toast.LENGTH_SHORT,
        ).show()
    } else {
        Toast.makeText(
            context,
            if (externalSaved) "PDF saved to Downloads area." else "PDF download is ready.",
            Toast.LENGTH_SHORT,
        ).show()
    }
}
