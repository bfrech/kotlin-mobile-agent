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
    var didHandler: DidHandler = DidHandler(this)


    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"

        try {
            ariesAgent = Ariesagent.new_(opts)
            val handler = NotificationHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered didExchange handler with registration id: $registrationID")

            val messagingRegistrationID = ariesAgent?.registerHandler(handler, "basicmessage")
            println("registered didExchange handler with registration id: $messagingRegistrationID")

        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    /*
        Mediator Functions
     */
    fun connectToMediator(mediatorUrl: String){
        this.mediatorURL = mediatorUrl
        mediator.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediator.registerMediator()
    }

    fun reconnect(){
        mediator.reconnectToMediator()
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
        didHandler.saveDIDInStore(did, name)
    }

    fun getDID(did: String){
        didHandler.getDID(did)
    }

    fun vdrResolveDID(did: String): String {
        return didHandler.vdrResolveDID(did)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createMyDID(): String {
        return didHandler.createMyDID()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTheirDID(didDoc: String): String {
        return didHandler.createTheirDID(didDoc)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDIDInVdr(didDoc: String): String {
        return didHandler.createDIDInVDR(didDoc)
    }

    fun storeDIDInVdr(didDoc: String): String {
        return didHandler.storeDIDInVDR(didDoc)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptConnectionInvitation(invitation: String): String{
        return connection.acceptConnectionInvitation(invitation)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createServiceEndpointInvitation(): String {
        return connection.createServiceEndpointInvitation()
    }


    fun createDIDExchangeInvitation(): String {
        return connection.createDIDExchangeInvitation()
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

    fun sendMessageViaTheirDID(message: String, theirDID: String){
        messaging.sendMessageViaTheirDID(message, theirDID)
    }

    fun sendMessageViaServiceEndpoint(message: String, serviceEndpoint: String){
        messaging.sendMessageViaServiceEndpoint(message, serviceEndpoint)
    }


}