package com.example.kotlin_agent.ariesAgent

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class ConnectionHandler(private val service: AriesAgent) {

    private val TAG = "ConnectionHandler"

    fun createDIDExchangeInvitation(): Pair<String, String> {
        val payload = """ {"alias": "${service.agentlabel}", "router_connection_id": "${service.routerConnectionId}"} """
        val invitationID = Utils.createUniqueID()
        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                Log.e(TAG, "Cannot create connection invitation: ${res.error}")
            } else {
                val actionsResponse = JSONObject(String(res.payload, StandardCharsets.UTF_8))
                val invitation = JSONObject(actionsResponse["invitation"].toString())

                return """{ "label": "${service.agentlabel}", 
                    |"serviceEndpoint": "${invitation["serviceEndpoint"]}", 
                    |"recipientKeys": ${invitation["recipientKeys"]}, 
                    |"routingKeys":  ${invitation["routingKeys"]},
                    |"invitationID": "$invitationID"
                    |}""".trimMargin()  to invitationID
            }
        }
        return "" to ""
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBInvitation(): String {

        val myDID = service.didHandler.createMyDID()
        val myDIDDoc = service.didHandler.vdrResolveDID(myDID)

        val payload = """ { "label": "${service.agentlabel}", "from": "$myDID", 
            |"body": {"accept": ["didcomm/v2"], "goal_code": "connect"},
            | "attachments": [{"id": "peer-connection-request", "mime-type": "application/json",
            | "data": {"base64": "${Utils.encodeBase64(myDIDDoc)}"}}]
            | } """.trimMargin()

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.outOfBandV2Controller?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                Log.e(TAG, "Cannot create outofband v2 invitation: ${res.error}")
            } else {
                return String(res.payload, StandardCharsets.UTF_8)
            }
        }
        return ""
    }


    fun acceptOOBV2Invitation(inv: String): String{

        val invitation = """ ${inv.dropLast(2)}, 
            |"my_label": "${service.agentlabel}", 
            |"my_router_connections": ["${service.routerConnectionId}"] 
            |}""".trimMargin()


        val outOfBandV2Controller = service.ariesAgent?.outOfBandV2Controller
        val data = invitation.toByteArray(StandardCharsets.UTF_8)
        if (outOfBandV2Controller != null) {
            val res = outOfBandV2Controller.acceptInvitation(data)
            if(res.error != null){
                Log.e(TAG, "Cannot accept outofband v2 invitation: ${res.error}")
            } else {
                return AriesUtils.extractValueFromJSONObject(
                    String(res.payload, StandardCharsets.UTF_8),
                    AriesUtils.CONNECTION_ID_KEY
                )
            }
        }
        return ""
    }

    fun getTheirDIDForConnection(connectionID: String): String {
        return AriesUtils.extractValueFromJSONObject(getConnection(connectionID), AriesUtils.THEIR_DID_KEY)
    }

    fun getConnection(connectionID: String): String{
        val request = """{"id": "$connectionID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.queryConnectionByID(data)
        if(res != null){
            if(res.error != null){
                Log.e(TAG, "Cannot get connection for $connectionID: ${res.error}")
            } else {
                return AriesUtils.extractValueFromJSONObject(
                    String(res.payload, StandardCharsets.UTF_8),
                    AriesUtils.RESULT_KEY
                )
            }
        }
        return ""
    }

    fun createNewConnection(myDID: String, theirDID: String): String {

        val request = """{"my_did": "$myDID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.createConnectionV2(data)
        if(res != null){
            if(res.error != null){
                Log.e(TAG, "Cannot create new connection: ${res.error}")
            } else {
                return AriesUtils.extractValueFromJSONObject(
                    String(res.payload, StandardCharsets.UTF_8),
                    AriesUtils.ID_KEY
                )
            }
        }
        return ""
    }

    fun updateTheirDIDForConnection(connectionID: String, theirDID: String): String {
        val request = """{"id": "$connectionID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.updateTheirDIDForConnection(data)
        if(res != null){
            if(res.error != null){
                Log.e(TAG, "Cannot update their did for connection $connectionID ${res.error}")
            }
            // Return empty response on success
        }
        return ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun rotateDIDForConnection(connectionID: String): String {

        val kid = getOldKidForConnection(connectionID)
        val newDID = service.didHandler.createMyDID()

        val request = """{"id": "$connectionID", "kid": "$kid" ,"new_did": "$newDID", "create_peer_did": false}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.rotateDID(data)
        if(res != null){
            if(res.error != null){
                Log.e(TAG, "Cannot rotate DID for connection: ${res.error}")
            } else {

                service.mediatorHandler.removeKeyFromMediator(kid)

                return AriesUtils.extractValueFromJSONObject(
                    String(res.payload, StandardCharsets.UTF_8),
                    AriesUtils.NEW_DID_KEY
                )
            }
        }
        return ""
    }

    fun getMyDIDForConnection(connectionID: String): String{
        return AriesUtils.extractValueFromJSONObject(
            getConnection(connectionID),
            AriesUtils.MY_DID_KEY
        )
    }

    private fun getOldKidForConnection(connectionID: String): String {
        val myDID = getMyDIDForConnection(connectionID)

        val methods = AriesUtils.extractValueFromJSONObject(
            service.didHandler.vdrResolveDID(myDID),
            AriesUtils.ASSERTION_METHOD_KEY
        )

       var kid = AriesUtils.extractValueFromJSONArray(methods, 0)

        if (kid.startsWith("#")) {
            val peerDID = getMyDIDForConnection(connectionID)
            kid = peerDID + kid
        }
        return kid
    }

}