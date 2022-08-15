package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Messaging(private val service: AriesAgent) {

    /*
       Register Service
     */
    fun registerMessagingService(name: String, purpose: String) {
        val messageController = service.ariesAgent?.messagingController
        val payload =
            """{"name":"$name", "purpose": ["$purpose"], "type": "https://didcomm.org/generic/1.0/message"} """
        println(payload)
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.registerService(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            }
        }

        getRegisteredServices()
    }

    private fun getRegisteredServices() {
        val messageController = service.ariesAgent?.messagingController
        val payload = """ {} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = messageController?.services(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {

                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("Registered Services: $actionsResponse")
            }
        }
    }

    fun sendMessage(message: String, connectionID: String){

        val theirDID = service.connection.getTheirDIDForConnection(connectionID)
        println(theirDID)

        val messageBody = """ {
			    "@type": "https://didcomm.org/basicmessage/2.0/message",
                "label": "${service.agentlabel}",
                "body": {
			        "content": "$message"
                }
			} """
        sendViaConnectionID(messageBody, connectionID)
    }

    private fun sendViaConnectionID(messageBody: String, connectionID: String){
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

    private fun sendViaTheirDID(messageBody: String, theirDID: String){
        val payload = """ {"message_body": $messageBody, "their_did": "$theirDID"} """
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



    /*
        Connection Messages Message
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendConnectionResponse(connectionID: String, purpose: String) {

        val connection = JSONObject(service.connection.getConnection(connectionID))
        val jsonInvitation = JSONObject(connection["invitation"].toString())
        val myDID = jsonInvitation["from"].toString()

        val oobInvitation = service.connection.createOOBResponse(myDID)

        val messageBody = """ {
			    "@type": "https://didcomm.org/connection/2.0/message",
                "purpose": "$purpose",
                "body": {
                    "content": "${Utils.encodeBase64(oobInvitation)}",
                    "label": "${service.agentlabel}"
                }
			} """

        sendViaConnectionID(messageBody, connectionID)
    }


    fun sendOOBInvitationViaServiceEndpoint(message: String, serviceEndpoint: String, purpose: String) {
        val messageBody = """ {
			    "@type": "https://didcomm.org/connection/2.0/message",
                "purpose": "$purpose",
                "body": {
			        "content": "$message",
                    "label": "${service.agentlabel}"
                }
			} """
        val payload = """ {"message_body": $messageBody, "service_endpoint": $serviceEndpoint} """
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.messagingController?.send(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            }
        }
    }


}
