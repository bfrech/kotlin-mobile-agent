package com.example.kotlin_agent.ariesAgent

import android.util.Log
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.StandardCharsets

class MediatorHandler(private val service: AriesAgent) {

    private val TAG = "MediatorHandler"

    fun connectToMediator(mediatorUrl: String): String{
        val url = URL(mediatorUrl)
        val connection = url.openConnection()

        val content = connection.getInputStream().bufferedReader().use(BufferedReader::readText)
        Log.d(TAG, "Got invitation from mediator: $content")
        val invite = """{ "invitation": $content, "my_label": "${service.agentlabel}" }"""

        val data = invite.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.outOfBandController?.acceptInvitation(data)
        if (res != null) {
            if(res.error != null){
                Log.e(TAG, "Could not accept mediator invitation: ${res.error.message}")
            } else {
                return AriesUtils.extractValueFromJSONObject(
                    String(res.payload, StandardCharsets.UTF_8),
                    AriesUtils.CONNECTION_ID_KEY
                )
            }
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
               Log.e(TAG,"There was an error with the Router Registration ${res.error.message}")
            } else {
                Log.d(TAG,"Registered Router with: ${service.routerConnectionId}")

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
                Log.e(TAG,"There was an error with the Router Reconnection ${res.error.message}")
            } else {
               Log.d(TAG, "Reconnected to Router with: ${service.routerConnectionId}")

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
                Log.d(TAG, "There was an error with the Key Registration ${res.error.message}")
            } else {
                Log.e(TAG, "Registered Key with mediator: $didKey")

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
                Log.e(TAG, "There was an error with the Key Removal ${res.error.message}")
            } else {
                Log.d(TAG, "Removed Key from mediator: $didKey")

            }
        }
    }
}