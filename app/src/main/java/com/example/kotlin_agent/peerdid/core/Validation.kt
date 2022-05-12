package com.example.kotlin_agent.peerdid.core

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.kotlin_agent.peerdid.VerificationMaterialPeerDID
import com.example.kotlin_agent.peerdid.VerificationMethodTypeAgreement
import com.example.kotlin_agent.peerdid.VerificationMethodTypeAuthentication
import com.example.kotlin_agent.peerdid.VerificationMethodTypePeerDID

internal fun validateAuthenticationMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAuthentication)
        throw IllegalArgumentException("Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAuthentication")
}

internal fun validateAgreementMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAgreement)
        throw IllegalArgumentException("Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAgreement")
}

internal fun validateJson(value: String) {
    val gson = Gson()
    try {
        gson.fromJson(value, Any::class.java)
    } catch (ex: JsonSyntaxException) {
        throw IllegalArgumentException("Invalid JSON $value", ex)
    }
    if (!value.contains("{")) throw IllegalArgumentException("Invalid JSON $value")
}

internal fun validateRawKeyLength(key: ByteArray) {
    // for all supported key types now (ED25519 and X25510) the expected size is 32
    if (key.size != 32)
        throw IllegalArgumentException("Invalid key $key")
}
