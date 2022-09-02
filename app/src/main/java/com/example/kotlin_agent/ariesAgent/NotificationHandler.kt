package com.example.kotlin_agent.ariesAgent

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import org.hyperledger.aries.api.Handler
import java.nio.charset.StandardCharsets


class NotificationHandler(private val ariesAgent: AriesAgent) : Handler {

    private val TAG = "NotificationHandler"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun handle(topic: String, message: ByteArray) {
        Log.d( TAG,"received notification topic: $topic" )
        Log.d( TAG, "received notification message: " + String(message, StandardCharsets.UTF_8) )


        val newMessage = AriesUtils.extractValueFromJSONObject(
            String(message, StandardCharsets.UTF_8),
            AriesUtils.MESSAGE_KEY
        )


        // Connection Request Handling
        if(topic == "connection_request"){
            Log.d( TAG, "received connection request: $newMessage" )

            ariesAgent.createAndSendConnectionResponse(
                Utils.decodeBase64(AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY))
            )
            return
        }

        // Connection Response Handling
        if(topic == "connection_response"){
            Log.d(TAG, "Received Connection Response: $newMessage" )
            ariesAgent.completeConnectionRequest(
                Utils.decodeBase64(AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY)),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.FROM_KEY)
            )
            return
        }

        // Connection Complete Handling
        if(topic == "connection_complete"){
            Log.d(TAG,"Received Connection Completion Acknowledgment: $newMessage" )
            ariesAgent.acknowledgeConnectionComplete(
                Utils.decodeBase64(AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY))
            )
            return
        }

        // Message Handling
        if(topic == "mobile_message"){
            Log.d(TAG, "Got a Message: $newMessage")
            ariesAgent.processIncomingMobileMessage(
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.FROM_KEY),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.TO_KEY),
                Utils.decodeBase64(AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY)),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CREATED_TIME_KEY)
            )
            return
        }


        // Mediator Connection Handling
        val stateID = AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.STATE_ID_KEY)
        if(stateID == "completed") {
            Log.d(TAG, "reached state ID completed, registering mediator now")
            ariesAgent.registerMediator()
        }
    }
}