package com.example.kotlin_agent.didcomm;

import com.example.kotlin_agent.ariesAgent.AriesAgentService
import org.didcommx.didcomm.DIDComm;

class DIDCommService(ariesService: AriesAgentService) {

    val didComm = DIDComm(MessagingDIDDocResolver(ariesService), MessagingSecretResolver(ariesService))


}
