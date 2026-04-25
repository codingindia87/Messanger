package com.codingindia.messanger.core.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    private const val ALGORITHM = "AES"

    // ध्यान दें: यह पासवर्ड ठीक 16 characters का होना चाहिए (128-bit)
    private const val MY_PASSWORD = "S@Achin1)(><?YK@"

    // 1. मैसेज को एन्क्रिप्ट करने वाला फंक्शन
    fun encrypt(plainText: String): String? {
        return try {
            val keySpec = SecretKeySpec(MY_PASSWORD.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray())
            // बाइनरी डेटा को स्ट्रिंग में बदलने के लिए Base64 का उपयोग करें
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 2. मैसेज को डिक्रिप्ट करने वाला फंक्शन
    fun decrypt(encryptedText: String): String? {
        return try {
            val keySpec = SecretKeySpec(MY_PASSWORD.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)

            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}