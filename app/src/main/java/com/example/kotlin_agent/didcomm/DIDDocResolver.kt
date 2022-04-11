package com.example.kotlin_agent.didcomm

import com.example.kotlin_agent.ariesAgent.AriesAgentService
import org.didcommx.didcomm.diddoc.DIDDoc
import org.didcommx.didcomm.diddoc.DIDDocResolver
import org.hyperledger.aries.models.RequestEnvelope
import java.nio.charset.StandardCharsets
import java.util.*

class MessagingDIDDocResolver(private val service: AriesAgentService): DIDDocResolver {

    override fun resolve(did: String): Optional<DIDDoc> {

        val vdrController = service.ariesAgent?.vdrController
        val vdrRequest = ""
        val data = vdrRequest.toByteArray(StandardCharsets.UTF_8)

        val res = vdrController?.resolveDID(data)
        if (res != null) {
            if(res.error != null) {
                println(res.error)
            } else {
                println(res.payload)
            }
        }

        TODO("Not yet implemented")

    }
}