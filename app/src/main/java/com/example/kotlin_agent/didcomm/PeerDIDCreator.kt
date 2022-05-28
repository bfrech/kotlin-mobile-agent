package com.example.kotlin_agent.didcomm

import com.example.kotlin_agent.ariesAgent.AriesAgent
import com.example.kotlin_agent.peerdid.*
import org.didcommx.didcomm.secret.SecretResolverEditable
import org.didcommx.didcomm.secret.generateEd25519Keys
import org.didcommx.didcomm.secret.generateX25519Keys
import org.didcommx.didcomm.secret.jwkToSecret
import org.didcommx.didcomm.utils.toJson
import org.json.JSONObject

/*
    Need to give source here (Adapted from: ...)
 */
class PeerDIDCreator( secretsResolver: SecretResolverEditable? = null ) {

    private val secretsResolver = secretsResolver ?: DIDCommSecretResolver()

    fun createPeerDID(
        authKeysCount: Int = 1,
        agreementKeysCount: Int = 1,
    ): String{

        val x25519keyPairs = (1..agreementKeysCount).map{ generateX25519Keys()}
        val ed25519keyPairs = (1..authKeysCount).map { generateEd25519Keys() }


        // Get the Router Connection to create Service Endpoint
        val routerConnection = AriesAgent.getInstance()?.getRouterConnection()
        val jsonRouterConnection = JSONObject(routerConnection)

        // Format Issue: new format return service endpoint as object, old format as simple string
        // New Format:
        val serviceEndpoint = jsonRouterConnection["ServiceEndPoint"].toString()

        // TODO: remove when obsolete. Old Format:
        val serviceEndpointOldJson = JSONObject(serviceEndpoint)
        val serviceEndpointOld = serviceEndpointOldJson["uri"].toString()

        // TODO: needs better handling of json Array here
        val serviceRoutingKeys = listOf(jsonRouterConnection["RecipientKeys"].toString().drop(1).dropLast(1))


        // 2. prepare the keys for peer DID lib
        val authPublicKeys = ed25519keyPairs.map {
            VerificationMaterialAuthentication(
                format = VerificationMaterialFormatPeerDID.JWK,
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                value = it.public
            )
        }
        val agreemPublicKeys = x25519keyPairs.map {
            VerificationMaterialAgreement(
                format = VerificationMaterialFormatPeerDID.JWK,
                type = VerificationMethodTypeAgreement.JSON_WEB_KEY_2020,
                value = it.public
            )
        }

        // 3. generate service
        val service = serviceEndpointOld?.let {
            toJson(
                DIDCommServicePeerDID(
                    id = "new-id",
                    type = SERVICE_DIDCOMM_MESSAGING,
                    serviceEndpoint = it,
                    routingKeys = serviceRoutingKeys,
                    accept = listOf("didcomm/v2")
                ).toDict()
            )
        }

        // 4. call peer DID lib
        // if we have just one key (auth), then use numalg0 algorithm
        // otherwise use numalg2 algorithm
        val did = if (authPublicKeys.size == 1 && agreemPublicKeys.isEmpty() && service.isNullOrEmpty())
            createPeerDIDNumalgo0(authPublicKeys[0])
        else
            createPeerDIDNumalgo2(
                signingKeys = authPublicKeys,
                encryptionKeys = agreemPublicKeys,
                service = service
            )

        // 5. set KIDs as in DID DOC for secrets and store the secret in the secrets resolver
        val didDoc = DIDDocPeerDID.fromJson(resolvePeerDID(did, VerificationMaterialFormatPeerDID.JWK))
        didDoc.agreementKids.zip(x25519keyPairs).forEach {
            val privateKey = it.second.private.toMutableMap()
            privateKey["kid"] = it.first
            secretsResolver.addKey(jwkToSecret(privateKey))
        }
        didDoc.authenticationKids.zip(ed25519keyPairs).forEach {
            val privateKey = it.second.private.toMutableMap()
            privateKey["kid"] = it.first
            secretsResolver.addKey(jwkToSecret(privateKey))
        }

        return did
    }
}