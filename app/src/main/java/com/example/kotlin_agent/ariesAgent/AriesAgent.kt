package com.example.kotlin_agent.ariesAgent

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

    var mediatorService: MediatorService = MediatorService(this)
    var connectionService: ConnectionService = ConnectionService(this)


    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = true
        opts.transportReturnRoute = "all"
        opts.label = agentlabel
        opts.addOutboundTransport("ws")
        opts.mediaTypeProfiles = "didcomm/v2"
        //opts.autoAccept = true  --> default value?

        try {
            ariesAgent = Ariesagent.new_(opts)
            val handler = ConnectionHandler(this)
            val registrationID = ariesAgent?.registerHandler(handler, "didexchange_states")
            println("registered handler with registration id: $registrationID")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun connectToMediator(mediatorUrl: String){
        this.mediatorURL = mediatorUrl
        mediatorService.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediatorService.registerMediator()
    }

    //@RequiresApi(Build.VERSION_CODES.O)
    //fun createOOBV2InvitationForMobileAgent() {
    //    connectionService.createOOBV2InvitationForMobileAgent("Connect", "connect")
    //}

    fun createOOBInvitationForMobileAgent() {
        connectionService.createOOBInvitationForMobileAgent()
    }

    fun createDIDExchangeRequest() {
        connectionService.createDIDExchangeRequest()
    }

    fun acceptDIDExchangeRequest(){
        connectionService.acceptDIDExchangeRequest()
    }

}