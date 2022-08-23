package com.example.kotlin_agent.ariesAgent

import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class AriesUtils {

    companion object {

        const val CONTENT_KEY = "content"
        const val MESSAGE_KEY = "message"
        const val FROM_KEY = "from"
        const val TO_KEY = "to"
        const val STATE_ID_KEY = "StateID"
        const val CONNECTION_ID_KEY = "connection_id"
        const val THEIR_DID_KEY = "TheirDID"
        const val MY_DID_KEY = "MyDID"
        const val RESULT_KEY = "result"
        const val ID_KEY = "id"
        const val NEW_DID_KEY = "new_did"
        const val ASSERTION_METHOD_KEY = "assertionMethod"
        const val DID_DOCUMENT_KEY = "didDocument"

        fun createPayloadData(payload: String): ByteArray {
            return payload.toByteArray(StandardCharsets.UTF_8)
        }



        fun extractValueFromJSONObject(objectString: String, key: String): String{
           val json = JSONObject(objectString)
           return json[key].toString()
        }

        fun extractValueFromJSONArray(arrayString: String, i: Int): String{
            val json = JSONArray(arrayString)
            return json[i].toString()
        }

    }
}