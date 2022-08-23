package com.example.kotlin_agent.ariesAgent

import org.json.JSONObject
import java.nio.charset.StandardCharsets

class KeyHandler(private val service: AriesAgent) {

    fun createVerificationMethod(keyType: String, type: String): String {
        val (key, keyDID) = createKeySet(keyType)
        service.mediatorHandler.addKeyToMediator(keyDID)
        return buildVerificationMethodFromKey(keyDID,type, key)
    }

    private fun createKeySet(keyType: String): Pair<String, String> {
        //DIDComm V2 Key Agreement: NISTP384ECDHKW or X25519ECDH
        val kmsController = service.ariesAgent?.kmsController
        val kmsRequest = """{ "keyType" : "$keyType" }"""
        val kmsData = kmsRequest.toByteArray(StandardCharsets.UTF_8)
        val kmsResponse = kmsController?.createKeyWithDIDKey(kmsData)
        var key = ""
        var keyDID = ""
        if (kmsResponse != null) {
            if (kmsResponse.error != null) {
                println(kmsResponse.error)
            } else {
                val actionsResponse = String(kmsResponse.payload, StandardCharsets.UTF_8)
                println("Created Key Set: $actionsResponse")
                val jsonActionResponse = JSONObject(actionsResponse)
                key = jsonActionResponse["publicKey"].toString()
                keyDID = jsonActionResponse["keyDID"].toString()
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