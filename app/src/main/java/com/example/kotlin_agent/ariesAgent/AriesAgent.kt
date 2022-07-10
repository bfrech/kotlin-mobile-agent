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

    var openDID = ""

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

            val connectionRegistrationID = ariesAgent?.registerHandler(handler, "connection")
            println("registered connection handler with registration id: $connectionRegistrationID")

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    /*
        Establish Connection
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionRequest(invitation: String){
        val myDID = didHandler.createMyDID()
        openDID = myDID
        val myDIDDoc = didHandler.vdrResolveDID(myDID)

        mediator.reconnectToMediator()
        messaging.sendMessageViaServiceEndpoint(Utils.encodeBase64(myDIDDoc), invitation)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAndSendConnectionResponse(didDocEnc: String, label: String){
        val didDoc = Utils.decodeBase64(didDocEnc)
        println("Got Connection Request from $label with theirDID: $didDoc")

        val theirDID = didHandler.createTheirDIDFromDoc(didDoc)
        val connectionID = connection.createNewConnection(openDID, theirDID)
        println("Created Connection with: $connectionID")

        // TODO: send message back with myDID and mylabel
        messaging.sendMessage(Utils.encodeBase64(openDID), connectionID)
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




    fun getConnection(connectionID: String): String {
        return connection.getConnection(connectionID)
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
        return didHandler.createTheirDIDFromDoc(didDoc)
    }



    fun createDIDExchangeInvitation(): String {
        return connection.createDIDExchangeInvitation()
    }




}