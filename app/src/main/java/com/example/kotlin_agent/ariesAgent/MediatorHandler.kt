package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.StandardCharsets

class MediatorHandler(private val service: AriesAgent) {

    fun connectToMediator(mediatorUrl: String): String{
        val url = URL(mediatorUrl)
        val connection = url.openConnection()

        val content = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
        val invite = """{ "invitation": $content, "my_label": "${service.agentlabel}" }"""

        var res: ResponseEnvelope
        try {
            val outOfBandController = service.ariesAgent?.outOfBandController
            val data = invite.toByteArray(StandardCharsets.UTF_8)
            if (outOfBandController != null) {
                res = outOfBandController.acceptInvitation(data)
                if(res.error != null){
                    println(res.error.message)
                } else {
                    return AriesUtils.extractValueFromJSONObject(
                        String(res.payload, StandardCharsets.UTF_8),
                        AriesUtils.CONNECTION_ID_KEY
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun registerMediator() {
        val mediatorController = service.ariesAgent?.mediatorController
        val mediatorRequest = """ {"connectionID":"${service.routerConnectionId}"} """

        val data = mediatorRequest.toByteArray(StandardCharsets.UTF_8)
        val res = mediatorController?.register(data)

        if (res != null) {
            if(res.error != null){
                println("There was an error with the Router Registration ${res.error.message}")
            } else {
                println("Registered Router with: ${service.routerConnectionId}")

            }
        }
    }


    fun reconnectToMediator() {
        val mediatorController = service.ariesAgent?.mediatorController
        val mediatorRequest = """ {"connectionID":"${service.routerConnectionId}"} """

        val data = mediatorRequest.toByteArray(StandardCharsets.UTF_8)
        val res = mediatorController?.reconnect(data)

        if (res != null) {
            if(res.error != null){
                println("There was an error with the Router Reconnection ${res.error.message}")
            } else {
                println("Reconnected to Router with: ${service.routerConnectionId}")

            }
        }
    }

    fun addKeyToMediator(didKey: String) {
        val mediatorController = service.ariesAgent?.mediatorController
        val request = """ {"connectionID":"${service.routerConnectionId}", "did_key": "$didKey"} """

        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = mediatorController?.registerKey(data)

        if (res != null) {
            if(res.error != null){
                println("There was an error with the Key Registration ${res.error.message}")
            } else {
                println("Registered Key with mediator: $didKey")

            }
        }
    }

    fun removeKeyFromMediator(didKey: String) {
        val mediatorController = service.ariesAgent?.mediatorController
        val request = """ {"connectionID":"${service.routerConnectionId}", "did_key": "$didKey"} """

        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = mediatorController?.unregisterKey(data)

        if (res != null) {
            if(res.error != null){
                println("There was an error with the Key Removal ${res.error.message}")
            } else {
                println("Removed Key from mediator: $didKey")

            }
        }
    }


}