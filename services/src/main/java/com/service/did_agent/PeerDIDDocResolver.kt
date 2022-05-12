package com.service.did_agent

import android.os.Build
import androidx.annotation.RequiresApi
import org.didcommx.peerdid.DIDDocPeerDID
import org.didcommx.peerdid.VerificationMaterialFormatPeerDID
import org.didcommx.peerdid.resolvePeerDID

class PeerDIDDocResolver{

    @RequiresApi(Build.VERSION_CODES.N)
    fun resolve(did: String): Map<String, Any> {

        val didDocJson = resolvePeerDID(did, format = VerificationMaterialFormatPeerDID.JWK)
        val didDoc = DIDDocPeerDID.fromJson(didDocJson)
        return didDoc.toDict()

    }
}