package com.example.kotlin_agent.ariesAgent

import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class KeyHandler(private val service: AriesAgent) {

    private val TAG = "KeyHandler"

    fun createVerificationMethod(keyType: String, type: String): String {
        val (key, keyDID) = createKeySet(keyType)
        service.mediatorHandler.addKeyToMediator(keyDID)
        return buildVerificationMethodFromKey(keyDID,type, key)
    }

    private fun createKeySet(keyType: String): Pair<String, String> {
        val kmsController = service.ariesAgent?.kmsController
        val kmsRequest = """{ "keyType" : "$keyType" }"""
        val kmsData = kmsRequest.toByteArray(StandardCharsets.UTF_8)
        val kmsResponse = kmsController?.createKeyWithDIDKey(kmsData)
        var key = ""
        var keyDID = ""
        if (kmsResponse != null) {
            if (kmsResponse.error != null) {
                Log.e(TAG, "Could not create key set ${kmsResponse.error}")
            } else {
                keyDID = AriesUtils.extractValueFromJSONObject(
                    String(kmsResponse.payload, StandardCharsets.UTF_8),
                    AriesUtils.KEY_DID_KEY
                )

                key = AriesUtils.extractValueFromJSONObject(
                    String(kmsResponse.payload, StandardCharsets.UTF_8),
                    AriesUtils.PUBLIC_KEY_KEY
                )
            }
        }
        return Pair(key, keyDID)
    }


    private fun buildVerificationMethodFromKey(id: String, keyType: String, value: String): String {
        return """
            {
                "id": "$id",
                "type":"$keyType",
                "controller":"#id",
                "publicKeyBase58": "$value"          
            }
        """.trimIndent()

    }

}