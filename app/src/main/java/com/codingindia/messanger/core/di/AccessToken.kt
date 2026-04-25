package com.codingindia.messanger.core.di

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {

    fun getAccessToken(): String?{
        try {
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"instagrama-25339\",\n" +
                    "  \"private_key_id\": \"e14b0f166b34e0d1d6360a64cff7ef8a3859547b\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC+uUVk5mPX4Ekt\\ntQg1IgFxjTj7Splhr3u7lcrRHRreyL0n2c+1UOAqkIrgcuvhyWUqndn6+Ou2HtQT\\np1uH08r4Lh6R9x/5S/OiPSgiHM9i7DUir4zWmoIfiBwsPFzUmhLR3ghTNH9p9d9S\\npQPRtjWr5gNB+XZuH8uvNUF1YY/YTWYUuHPmrfwCWsZOgX944eaOW0c2pT6DPIwN\\nlGnNizdTsMoY3xthdahNP+Ka1wPG3JoZmoy3NVgZyFkoBjm/5VoDod+ffc5BUhT6\\ndwqeejT2fQfPmI6wCi2amyQDgC15B6vW8IsVLz8G2NDiuTwh8DNmZaSNWNJD24BS\\nWQwOxSSJAgMBAAECggEAAUfdDC4oNfslf/x9o15eLEfH6Jdp5m1x/dgQk60ScN61\\nYcw1OEZkgaH/rhL/fAA+S8C3GxfAUh8CCrwT4dqIJqNSpRAdLhn2jHHvhXke4w8V\\n1UI2Bnvk9fa+mlpxFM67DnSN/EG3kvvUcH7iAsQVN1lfpLoFiPxG1x9FrawxIIFI\\n5JVWfLglTYiv62J/Ybxh+ly1oxsrWc/SA8bHWtxQ/xtApo0ZYdXu4kUYz3oXasD/\\n6UxQgEE/76USHrivBF+KdBmcye/hle9yLEx6JhvQXRV1YmebOs/+YBhr5+Ya9HUn\\nHAatTGIV0DOSm2EIjkF9yJy139gtpKz4mjFW7kQqwQKBgQDDl7niVqg8NaO2/0tW\\njWPDleHBxFQwQWGwFrlv18C76LtaNHE5LDA8DgafZRn2gFJJHuJMYYuaOCQile0M\\nMHk5iZrdk/fXk2lCaKfomESCiJ4vpS+9JeLbW42q3c6ud192i1k1jWO6wfn+Hymb\\npVro1dJ5JfVwbwmzy2QFIzxg6QKBgQD5oJY1O05/j9Xec9vjdsE/ei73U4BxA139\\nZ5Wg1EqjT0eZZ5rDLFBMkjmm7g0yA1pf7jojNXmK8kO867LaPvexMQh6em8FeWRS\\n76rnUZxu3XfNudq046e4kzpDltQ9xSUwGe5N+1TtiW7PfuLlyLYJsUylMPfF+FWK\\nHf4d+ztioQKBgQCIN4DjmsprtEeiSMXlL28NZbLmSZ8ARGiORCU9ORsnQvxH5EH+\\nOUIWcQY3uCeOvuuPtQyReVXKP9MaqyN927xxT94k9soivq0N7OkTWghMiGzyba6D\\nooENmANfvj4Uz1oSqxWj8CZGiJVcX8OQfGaFxXRhnwgCF7LAHqQ318RTEQKBgQD4\\ncVVOAYqs9rC6x8DPcIAA91ALI/YvhFRlQlTvTppCU0NFcJHdMhtxOg9bDo3feurV\\nkzoVlME1As1cF5FYGVdX6R8xFu2sGxc4XARSWS1CQfVcsV70radd0loortLp726F\\noylEP6JVD3VT2ktkuxBCMU3BEy1mwTQRVKx3lnWSQQKBgQCzDQ8XOX8mN9+GKDEE\\nfEvPwPNxiEGvm6I08OdppdJTIBDBQBVstSsL4giGs6TnToVW+qM6xxqmSeWxq+u5\\nbHK8t5LVkgIRuFkATYYEhNK4zUveeacyZ2530TvjTilaQRMEOTkOgpAMQrPPDULv\\n3rCnHsXUhnu189Q00ugFMBJiEQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-281fg@instagrama-25339.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"111531048217314473337\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-281fg%40instagrama-25339.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf("https://www.googleapis.com/auth/firebase.messaging"))

            googleCredentials.refresh()

            return googleCredentials.accessToken.tokenValue
        }catch (e: IOException){
            return null
        }
    }
}