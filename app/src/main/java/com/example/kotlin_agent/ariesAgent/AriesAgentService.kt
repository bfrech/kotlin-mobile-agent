package com.example.kotlin_agent.ariesAgent

import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.json.JSONObject
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.StandardCharsets

class AriesAgentService {

    var ariesAgent: AriesController? = null
    var useLocalAgent: Boolean = true
    var agentlabel: String = ""
    var routerConnectionId = ""

    var mediatorService: MediatorService = MediatorService(this)
    var connectionService: ConnectionService = ConnectionService(this)

    fun createNewAgent(label: String) {
        agentlabel = label
        val opts = Options()
        opts.useLocalAgent = useLocalAgent
        opts.transportReturnRoute = "all"
        opts.label = label
        opts.addOutboundTransport("ws")
        //opts.autoAccept = true

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
        mediatorService.connectToMediator(mediatorUrl)
    }

    fun registerMediator() {
        mediatorService.registerMediator()
    }

    fun createInvitationForMobileAgent() {
        connectionService.createInvitationForMobileAgent("Connect", "connect")
    }

}