package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Connection(private val service: AriesAgent) {

    var openDID = ""


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
    fun createServiceEndpointInvitation(): String {
        val did = service.createMyDID()
        openDID = did
        val res = JSONObject(service.vdrResolveDID(did))
        val didDoc = JSONObject(res["didDocument"].toString())
        val service = JSONObject(didDoc["service"].toString().drop(1).dropLast(1))
        val serviceEndpoint = JSONObject(service["serviceEndpoint"].toString())
        val key = service["recipientKeys"].toString().drop(2).substringBefore('#')


        val payload = """{
            |"serviceEndpoint": "${serviceEndpoint["uri"]}",
            |"routingKeys": ${service["routingKeys"]},
            |"recipientKeys": "$key"
            |}""".trimMargin()


        return payload
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptConnectionInvitation(invitation: String): String {
        val theirDID = service.createTheirDID(invitation)

        // TODO: Test, remove
        println("My DID: ${service.vdrResolveDID(openDID)}")
        println("Their DID: ${service.vdrResolveDID(theirDID)}")

        //return createNewConnection(openDID, theirDID)
        return theirDID
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




}