package com.codingindia.messanger.core.repository

import com.codingindia.messanger.core.api.CallingAPI
import com.codingindia.messanger.core.notification.models.Call
import com.codingindia.messanger.core.notification.models.CallNotification
import jakarta.inject.Inject

class VideoCallRepository @Inject constructor(
    private val callingAPI: CallingAPI
) {

    suspend fun startCall(name: String, token: String, uid: String) {
        callingAPI.sendRing(
            CallNotification(
                message = Call(
                    token = token, data = mutableMapOf(
                        "title" to "VIDEO_CALL",
                        "body" to "VIDEO_CALL",
                        "uid" to uid,
                        "Type" to "OFFER_VIDEO_CALL",
                        "callerName" to name
                    )
                )
            )
        )
    }

    suspend fun endCall(token: String, uid: String){
        callingAPI.sendRing(
            CallNotification(
                message = Call(
                    token = token, data = mutableMapOf(
                        "title" to "VIDEO_CALL",
                        "body" to "VIDEO_CALL",
                        "uid" to uid,
                        "Type" to "CANCEL_VIDEO_CALL"
                    )
                )
            )
        )
    }
}