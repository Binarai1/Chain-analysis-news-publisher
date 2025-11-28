package com.raptoreum.report

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

enum class ShareTarget(val packageName: String?, val label: String) {
    FACEBOOK("com.facebook.katana", "Facebook"),
    X("com.twitter.android", "X"),
    LINKEDIN("com.linkedin.android", "LinkedIn"),
    REDDIT("com.reddit.frontpage", "Reddit"),
    GENERIC(null, "Share")
}

class ShareHelper {
    fun shareText(context: Context, text: String, target: ShareTarget) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            target.packageName?.let { setPackage(it) }
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share report via ${target.label}"))
        } catch (e: ActivityNotFoundException) {
            // fallback to a generic chooser if a specific app is not found
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share report"))
        }
    }

    fun sharePdf(context: Context, uri: Uri, target: ShareTarget) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            target.packageName?.let { setPackage(it) }
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share PDF via ${target.label}"))
        } catch (e: ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share PDF"))
        }
    }
}
