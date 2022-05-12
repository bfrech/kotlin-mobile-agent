package com.example.kotlin_agent.didcomm

import android.os.Build
import androidx.annotation.RequiresApi

import org.didcommx.didcomm.diddoc.DIDDoc
import org.didcommx.didcomm.diddoc.DIDDocResolver

import org.didcommx.didcomm.diddoc.VerificationMethod
import org.didcommx.didcomm.common.VerificationMethodType

import org.didcommx.didcomm.utils.toJson
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import com.example.kotlin_agent.peerdid.DIDCommServicePeerDID
import com.example.kotlin_agent.peerdid.DIDDocPeerDID
import com.example.kotlin_agent.peerdid.VerificationMaterialFormatPeerDID
import com.example.kotlin_agent.peerdid.resolvePeerDID
import java.util.*

class PeerDIDDocResolver(): DIDDocResolver {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun resolve(did: String): Optional<DIDDoc> {

        val didDocJson = resolvePeerDID(did, format = VerificationMaterialFormatPeerDID.JWK)
        val didDoc = DIDDocPeerDID.fromJson(didDocJson)

        didDoc.keyAgreement
        return Optional.ofNullable(
            DIDDoc(
                did = did,
                keyAgreements = didDoc.agreementKids,
                authentications = didDoc.authenticationKids,
                verificationMethods = (didDoc.authentication + didDoc.keyAgreement).map {
                    VerificationMethod(
                        id = it.id,
                        type = VerificationMethodType.JSON_WEB_KEY_2020,
                        controller = it.controller,
                        verificationMaterial = VerificationMaterial(
                            format = VerificationMaterialFormat.JWK,
                            value = toJson(it.verMaterial.value)
                        )
                    )
                },
                didCommServices = didDoc.service
                    ?.map {
                        when (it) {
                            is DIDCommServicePeerDID ->
                                DIDCommService(
                                    id = it.id,
                                    serviceEndpoint = it.serviceEndpoint ?: "",
                                    routingKeys = it.routingKeys ?: emptyList(),
                                    accept = it.accept ?: emptyList()
                                )
                            else -> null
                        }
                    }
                    ?.filterNotNull()
                    ?: emptyList()
            )
        )

    }
}