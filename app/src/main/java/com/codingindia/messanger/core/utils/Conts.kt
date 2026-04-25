package com.codingindia.messanger.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.graphics.Color as AndroidColor

object Conts {

    const val SECRET_KEY = "SachinVishwakarmaSampattiSataKey" // 32 chars
    const val IV = "SataAppStaticIV1" // 16 chars

    fun openCustomTab(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()

        builder.setToolbarColor(AndroidColor.parseColor("#03A9F4"))

        builder.setShowTitle(true)

        val customTabsIntent = builder.build()

        try {
            val uri = if (url.startsWith("http")) Uri.parse(url)
            else Uri.parse("http://$url")
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    fun formatLastSeen(lastSeen: Long?): String {
        if (lastSeen == null) return ""

        val now = System.currentTimeMillis()
        val diffMillis = now - lastSeen

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffMillis < TimeUnit.MINUTES.toMillis(1) -> "Last seen just now"
            minutes < 60 -> "Last seen $minutes minutes ago"
            hours < 24 -> "Last seen $hours hours ago"
            days == 1L -> {
                val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault()) // e.g. 09:20 PM
                val time = timeFormat.format(lastSeen)
                "Last seen yesterday at $time"
            }

            days < 7 -> {
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())       // e.g. Mon, Tue
                val timeFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                val day = dayFormat.format(lastSeen)
                val time = timeFormat.format(lastSeen)
                "Last seen $day at $time"
            }

            else -> {
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm aa", Locale.getDefault())
                "Last seen ${dateFormat.format(lastSeen)}"
            }
        }
    }
}