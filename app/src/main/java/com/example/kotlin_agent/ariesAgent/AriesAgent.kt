package com.example.kotlin_agent.ariesAgent

import android.os.Build
import androidx.annotation.RequiresApi
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options


class AriesAgent {

    companion object {
        private var INSTANCE: AriesAgent? = null

        fun getInstance(): AriesAgent? {
            if (INSTANCE == null) {
                INSTANCE = AriesAgent()
            }
            return INSTANCE
        }
    }

    var ariesAgent: AriesController? = null
    var agentlabel: String = ""
    var routerConnectionId = ""
    var mediatorURL = ""

    var mediator: Mediator = Mediator(this)
    var connection: Connection = Connection(this)


    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"

        //opts.mediaTypeProfiles = "didcomm/aip1"
        //opts.autoAccept = true  --> default value?

        try {
            ariesAgent = Ariesagent.new_(opts)
            val handler = NotificationHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered handler with registration id: $registrationID")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun connectToMediator(mediatorUrl: String){
        this.mediatorURL = mediatorUrl
        mediator.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediator.registerMediator()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createOOBV2InvitationForMobileAgent() {
      connection.createOOBV2InvitationForMobileAgent("Connect", "connect")
    }

    //fun createOOBInvitationForMobileAgent() {
    //    connection.createOOBInvitationForMobileAgent()
    //}

    //fun acceptDIDExchangeRequest(connectionID: String, theirLabel: String){
    //    println("AcceptDIDExchangeRequest was called")
    //    connection.acceptDIDExchangeRequest(connectionID, theirLabel)
    //}

    fun getConnection(connectionID: String){
        connection.getConnection(connectionID)
    }

    fun getRouterConnection(): String {
        return connection.getConnection(routerConnectionId)
    }

    fun createNewConnection(myDID: String, theirDID: String) {
        connection.createNewConnection(myDID, theirDID)
    }

    fun saveDID(did: String, name: String){
        connection.saveDIDInStore(did, name)
    }

    fun getDID(did: String){
        connection.getDID(did)
    }

    fun vdrResolveDID(did: String): String {
        return connection.vdrResolveDID(did)
    }

    fun createDIDInVdr(didDoc: String): String {
        return connection.createDIDInVDR(didDoc)
    }

    fun storeDIDInVdr(didDoc: String): String {
        return connection.storeDIDInVDR(didDoc)
    }
}