package com.example.kotlin_agent.ariesAgent

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

    fun getConnectionID(theirDID: String): String {
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


    fun rotateDIDForConnection(connectionID: String): String {

        val kid = getOldKidForConnection(connectionID)

        // TODO: pass whole service Endpoint, not single values
        val serviceEndpoint = JSONObject(getServiceEndpointForConnection(connectionID))
        println(serviceEndpoint)

        val request = """{"id": "$connectionID", "kid": "$kid" ,"new_did": "", "create_peer_did": true, "service_endpoint": "${serviceEndpoint["uri"]}", "routing_keys": ${serviceEndpoint["routingKeys"]}}"""
        println(request)

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