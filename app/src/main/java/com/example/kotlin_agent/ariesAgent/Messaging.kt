package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import java.nio.charset.StandardCharsets

class Messaging(private val service: AriesAgent) {

    //fun sendMessage(message: String, connectionID: String, goal: String){
    //    val theirDID = service.connection.getTheirDIDForConnection(connectionID)
    //    val messageBody = """ {
	//		    "@type": "https://didcomm.org/mobilemessage/1.0/message",
    //            "goal": "$goal",
    //            "to": "$theirDID",
    //            "body": {
	//		        "content": "$message"
    //            }
	//		} """
    //    sendMessageViaConnectionID(messageBody, connectionID)
    //}

    private fun sendMessageViaConnectionID(goal: String, message: String, connectionID: String){
        val messageBody = buildMessageBody(goal, message, connectionID)
        val payload = """ {"message_body": $messageBody, "connection_id": "$connectionID"} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.messagingController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                // Returns empty JSON
            }
        }
    }

    fun sendMessageViaServiceEndpoint(goal: String, message: String, serviceEndpoint: String) {
        val messageBody = buildMessageBody(goal, message, "")
        val payload = """ {"message_body": $messageBody, "service_endpoint": $serviceEndpoint} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.messagingController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendConnectionResponse(connectionID: String, goal: String) {
        val myDID = service.connection.getMyDIDForConnection(connectionID)
        //val oobInvitation = service.connection.createOOBResponse(myDID)
        sendMessageViaConnectionID(goal, service.agentlabel , connectionID)
    }

    fun sendMobileMessage(message: String, connectionID: String) {
        sendMessageViaConnectionID("mobile_message", message,  connectionID)
    }


    private fun buildMessageBody(goal: String, message: String, connectionID: String): String{
        val theirDID = if (connectionID != "") service.connection.getTheirDIDForConnection(connectionID) else ""
        return """ {
			    "@type": "https://didcomm.org/mobilemessage/1.0/message",
                "goal": "$goal",
                "to": "$theirDID",
                "body": {
			        "content": "$message"
                }
			} """
    }

}
