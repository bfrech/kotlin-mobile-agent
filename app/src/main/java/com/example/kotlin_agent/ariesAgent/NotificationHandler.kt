package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.api.Handler
import java.nio.charset.StandardCharsets

class NotificationHandler(private val ariesAgent: AriesAgent) : Handler {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun handle(topic: String, message: ByteArray) {
        println("received notification topic: $topic")
        println("received notification message: " + String(message, StandardCharsets.UTF_8))


        val newMessage = AriesUtils.extractValueFromJSONObject(
            String(message, StandardCharsets.UTF_8),
            AriesUtils.MESSAGE_KEY
        )


        // Connection Request Handling
        if(topic == "connection_request"){
            println("Received Connection Request: $newMessage" )

            ariesAgent.createAndSendConnectionResponse(
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY)
            )
            return
        }

        // Connection Response Handling
        if(topic == "connection_response"){
            println("Received Connection Response: $newMessage" )
            ariesAgent.completeConnectionRequest(
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.FROM_KEY)
            )
            return
        }

        // Connection Complete Handling
        if(topic == "connection_complete"){
            println("Received Connection Completion Acknowledgment: $newMessage" )
            ariesAgent.acknowledgeConnectionComplete(
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY)
            )
            return
        }

        // Message Handling
        if(topic == "mobile_message"){
            println("Got a Message: $newMessage")
            ariesAgent.processMobileMessage(
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.FROM_KEY),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.TO_KEY),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CONTENT_KEY),
                AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.CREATED_TIME_KEY)
            )
            return
        }


        // Mediator Connection Handling
        val stateID = AriesUtils.extractValueFromJSONObject(newMessage, AriesUtils.STATE_ID_KEY)
        if(stateID == "completed") {
            ariesAgent.registerMediator()
        }
    }
}