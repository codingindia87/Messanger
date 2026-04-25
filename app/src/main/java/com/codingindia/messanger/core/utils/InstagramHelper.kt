package com.codingindia.messanger.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object InstagramHelper {
    
    suspend fun extractVideoUrl(reelUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            // इंस्टाग्राम को यह दिखाने के लिए कि हम एक मोबाइल ब्राउज़र हैं
            val response = Jsoup.connect(reelUrl)
                .userAgent("Mozilla/5.0 (Linux; Android 10; SM-G981B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.162 Mobile Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.5")
                .timeout(10000)
                .get()

            // इंस्टाग्राम वीडियो का लिंक अक्सर इन मेटा टैग्स में होता है
            var videoUrl = response.select("meta[property=og:video]").attr("content")
            
            if (videoUrl.isEmpty()) {
                videoUrl = response.select("meta[property=og:video:secure_url]").attr("content")
            }
            
            if (videoUrl.isEmpty()) {
                // अगर मेटा टैग नहीं मिलता, तो कभी-कभी यह स्क्रिप्ट टैग के अंदर होता है
                val scripts = response.select("script")
                for (script in scripts) {
                    val data = script.data()
                    if (data.contains("video_url")) {
                        // यहाँ थोड़ा सा Regex का जादू चाहिए होगा (Advance parsing)
                        val regex = """video_url":"([^"]+)""".toRegex()
                        val match = regex.find(data)
                        videoUrl = match?.groupValues?.get(1)?.replace("\\u0026", "&") ?: ""
                        if (videoUrl.isNotEmpty()) break
                    }
                }
            }

            videoUrl.ifEmpty { null }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}