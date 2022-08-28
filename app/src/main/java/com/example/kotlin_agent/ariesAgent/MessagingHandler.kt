package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import java.nio.charset.StandardCharsets

class MessagingHandler(private val service: AriesAgent) {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessageViaConnectionID(goal: String, message: String, connectionID: String){
        val messageBody = buildMessageBody(goal, message, connectionID)
        val payload = """ {"message_body": $messageBody, "connection_id": "$connectionID"} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.messagingController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } // returns empty json on success
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessageViaServiceEndpoint(goal: String, message: String, serviceEndpoint: String) {
        val messageBody = buildMessageBody(goal, message, "")
        val payload = """ {"message_body": $messageBody, "service_endpoint": $serviceEndpoint} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.messagingController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } // returns empty json on success
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendConnectionResponse(connectionID: String, goal: String) {
        sendMessageViaConnectionID(goal, service.agentlabel , connectionID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMobileMessage(message: String, connectionID: String) {
        sendMessageViaConnectionID("mobile_message", message,  connectionID)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildMessageBody(goal: String, message: String, connectionID: String): String{
        val time = Utils.getCurrentTimeAsIsoString()
        println(time)
        val theirDID = if (connectionID != "") service.connectionHandler.getTheirDIDForConnection(connectionID) else ""
        return """ {
			    "@type": "https://didcomm.org/mobilemessage/1.0/message",
                "created_time": "$time",
                "goal": "$goal",
                "to": "$theirDID",
                "body": {
			        "content": "$message"
                }
			} """
    }

}
