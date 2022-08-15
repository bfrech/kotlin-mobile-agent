package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_agent.Utils
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Connection(private val service: AriesAgent) {

    fun createDIDExchangeInvitation(): String {
        val payload = """ {"alias": "${service.agentlabel}", "router_connection_id": "${service.routerConnectionId}"} """

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                val actionsResponse = JSONObject(String(res.payload, StandardCharsets.UTF_8))
                val invitation = JSONObject(actionsResponse["invitation"].toString())
                return """{ "label": "${service.agentlabel}", "serviceEndpoint": "${invitation["serviceEndpoint"]}", "recipientKeys": ${invitation["recipientKeys"]}, "routingKeys":  ${invitation["routingKeys"]}}"""
            }
        }
        return ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBInvitation(): String {

        val myDID = service.didHandler.createMyDID()
        val myDIDDoc = service.didHandler.vdrResolveDID(myDID)

        val payload = """ { "label": "${service.agentlabel}", "from": "$myDID", 
            |"body": {"accept": ["didcomm/v2"], "goal_code": "connect"},
            | "attachments": [{"id": "request-0", "mime-type": "application/json", "description": "didDoc",
            | "data": {"base64": "${Utils.encodeBase64(myDIDDoc)}"}}]
            | } """.trimMargin()

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.outOfBandV2Controller?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
            } else {
                return String(res.payload, StandardCharsets.UTF_8)
            }
        }
        return ""
    }


    fun createOOBResponse(myDID: String): String {

        val payload = """ { "label": "${service.agentlabel}", "from": "$myDID", 
            |"body": {"accept": ["didcomm/v2"], "goal_code": "connect"}
            | } """.trimMargin()

        val data = payload.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.outOfBandV2Controller?.createInvitation(data)
        if (res != null) {
            if (res.error != null) {
                println(res.error)
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

        var res: ResponseEnvelope
        try {
            val outOfBandV2Controller = service.ariesAgent?.outOfBandV2Controller
            val data = invitation.toByteArray(StandardCharsets.UTF_8)
            if (outOfBandV2Controller != null) {
                res = outOfBandV2Controller.acceptInvitation(RequestEnvelope(data))
                if(res.error != null){
                    println(res.error.message)
                } else {
                    val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                    val jsonActionResponse = JSONObject(actionsResponse)
                    return jsonActionResponse["connection_id"].toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


    //@RequiresApi(Build.VERSION_CODES.O)
    //fun createServiceEndpointInvitation(): String {
    //    val did = service.createMyDID()
    //    openDID = did
    //    val res = JSONObject(service.vdrResolveDID(did))
    //    val didDoc = JSONObject(res["didDocument"].toString())
    //    val service = JSONObject(didDoc["service"].toString().drop(1).dropLast(1))
    //    val serviceEndpoint = JSONObject(service["serviceEndpoint"].toString())
    //    val key = service["recipientKeys"].toString().drop(2).substringBefore('#')
    //    val payload = """{
    //        |"serviceEndpoint": "${serviceEndpoint["uri"]}",
    //        |"routingKeys": ${service["routingKeys"]},
    //        |"recipientKeys": "$key"
    //        |}""".trimMargin()
    //    return payload
    //}


    fun getTheirDIDForConnection(connectionID: String): String {
        val connection = JSONObject(getConnection(connectionID))
        return connection["TheirDID"].toString()
    }

    fun getConnection(connectionID: String): String{
        val request = """{"id": "$connectionID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.didExchangeController?.queryConnectionByID(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                val result = JSONObject(jsonObject["result"].toString())
                return result.toString()
            }
        } else {
            return "Res is null"
        }
        return ""
    }

    fun createNewConnection(myDID: String, theirDID: String): String {

        val request = """{"my_did": "$myDID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.createConnectionV2(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                return jsonObject["id"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }

    fun getConnectionIDByTheirDID(theirDID: String): String {
        val request = """{"their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.getConnectionIdByTheirDID(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                return jsonObject["id"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }

    fun updateTheirDIDForConnection(connectionID: String, theirDID: String): String {
        val request = """{"id": "$connectionID", "their_did": "$theirDID"}"""
        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.updateTheirDIDForConnection(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                println("Updated Connection: $actionsResponse")
            }
        } else {
            println("Res is null")
        }
        return ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun rotateDIDForConnection(connectionID: String): String {

        val kid = getOldKidForConnection(connectionID)

        // TODO: remove key here? is this good?
        service.mediator.removeKeyFromMediator(kid)

        val newDID = service.didHandler.createMyDID()

        val request = """{"id": "$connectionID", "kid": "$kid" ,"new_did": "$newDID", "create_peer_did": false}"""

        val data = request.toByteArray(StandardCharsets.UTF_8)
        val res = service.ariesAgent?.connectionController?.rotateDID(data)
        if(res != null){
            if(res.error != null){
                println(res.error)
            } else {
                val actionsResponse = String(res.payload, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(actionsResponse)
                println("Rotate DID response: $jsonObject")
                return jsonObject["new_did"].toString()
            }
        } else {
            println("Res is null")
        }
        return ""
    }

    fun getMyDIDForConnection(connectionID: String): String{
        val connection = JSONObject(getConnection(connectionID))
        return connection["MyDID"].toString()
    }

    private fun getOldKidForConnection(connectionID: String): String {
        val myDID = getMyDIDForConnection(connectionID)
        val myDIDDoc = JSONObject(service.didHandler.vdrResolveDID(myDID))
        val assertionMethod = JSONArray(myDIDDoc["assertionMethod"].toString())
        return assertionMethod[0].toString()
    }

    private fun getServiceEndpointForConnection(connectionID: String): String {
        val services = JSONArray(getServiceForConnection(connectionID))
        val service = JSONObject(services[0].toString())
        return service["serviceEndpoint"].toString()
    }

    private fun getServiceForConnection(connectionID: String): String {
        val myDID = getMyDIDForConnection(connectionID)
        val myDIDDoc = JSONObject(service.didHandler.vdrResolveDID(myDID))
        return myDIDDoc["service"].toString()
    }



}