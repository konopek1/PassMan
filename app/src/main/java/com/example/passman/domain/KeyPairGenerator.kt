package com.example.passman.domain

import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator

data class RSAKeyPair(val privateKey:String, val publicKey: String)

class KeyPairGenerator {
    fun generate(): RSAKeyPair {
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)

        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val keyPair: KeyPair = kpg.genKeyPair()
        val publicKey: ByteArray = keyPair.private.encoded
        val secretKey: ByteArray = keyPair.public.encoded

        return RSAKeyPair(secretKey.decodeToString(),publicKey.decodeToString())
    }


}