package com.codingindia.messanger.core.api

import com.codingindia.messanger.core.notification.models.CallNotification
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CallingAPI {

    @POST("/v1/projects/<YOUR_PROJECT_ID>/messages:send")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    suspend fun sendRing(
        @Body notification: CallNotification,
    ): Response<CallNotification>

}