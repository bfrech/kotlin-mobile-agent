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
    var messaging: Messaging = Messaging(this)
    var didExchange: DidExchange = DidExchange(this)


    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"

        //opts.mediaTypeProfiles = "didcomm/aip1"
        //opts.autoAccept = false  //--> default value?

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

    fun getConnection(connectionID: String): String {
        return connection.getConnection(connectionID)
    }

    fun getRouterConnection(): String {
        return connection.getConnection(routerConnectionId)
    }

    fun createNewConnection(myDID: String, theirDID: String): String {
        return connection.createNewConnection(myDID, theirDID)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createMyDID(): String {
        return connection.createMyDID()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTheirDID(didDoc: String): String {
        return connection.createTheirDID(didDoc)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDIDInVdr(didDoc: String): String {
        return connection.createDIDInVDR(didDoc)
    }

    fun storeDIDInVdr(didDoc: String): String {
        return connection.storeDIDInVDR(didDoc)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptConnectionInvitation(invitation: String): String{
        return connection.acceptConnectionInvitation(invitation)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createServiceEndpointInvitation(): String {
        return connection.createServiceEndpointInvitation()
    }


    /*
        Messaging Functions
     */
    fun registerService(name: String, purpose: String){
        println("Register Service Called")
        messaging.registerMessagingService(name, purpose)
    }

    fun sendMessage(message: String, connectionID: String){
        messaging.sendMessage(message, connectionID)
    }

    fun sendMessageViaServiceEndpoint(message: String, serviceEndpoint: String){
        messaging.sendMessageViaServiceEndpoint(message, serviceEndpoint)
    }

    fun createDidExchangeInvitation(): String {
        return didExchange.createDidExchangeInvitation()
    }

    /*
    fun receiveDidExchangeInvitation(invitation: String): String {
        return didExchange.receiveDidExchangeInvitation(invitation)
    }

    fun acceptDidExchangeInvitation(connectionID: String): String {
        return didExchange.acceptDidExchangeInvitation(connectionID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptDidExchangeRequest(connectionID: String): String {
        return didExchange.acceptDidExchangeRequest(connectionID)
    }

     */
}