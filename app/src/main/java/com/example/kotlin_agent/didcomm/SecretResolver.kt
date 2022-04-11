package com.example.kotlin_agent.didcomm

import com.example.kotlin_agent.ariesAgent.AriesAgentService
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolver
import java.util.*

class MessagingSecretResolver(val service: AriesAgentService): SecretResolver {
    override fun findKey(kid: String): Optional<Secret> {
        TODO("Not yet implemented")
    }

    override fun findKeys(kids: List<String>): Set<String> {
        TODO("Not yet implemented")
    }
}