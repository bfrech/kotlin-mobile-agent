package com.example.kotlin_agent.didcomm

import android.os.Build
import androidx.annotation.RequiresApi
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolver
import org.didcommx.didcomm.secret.SecretResolverEditable
import java.util.*
import kotlin.collections.HashMap

class DIDCommSecretResolver: SecretResolverEditable {

    // TODO: Make more sophisticated
    private val secrets = HashMap<String, Secret>()

    override fun addKey(secret: Secret) {
        secrets.put(secret.kid , secret)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun findKey(kid: String): Optional<Secret> {
        return Optional.ofNullable(secrets[kid])
    }

    override fun findKeys(kids: List<String>): Set<String> {
        TODO("Not yet implemented")
    }

    override fun getKids(): List<String> {
        return secrets.keys.toList()
    }

}