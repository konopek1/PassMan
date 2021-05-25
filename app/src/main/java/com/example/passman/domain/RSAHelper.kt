package com.example.passman.domain

import android.util.Log
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class RSAHelper {

    fun encrypt(key: String, value: String) {
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")

        val keyFactory = KeyFactory.getInstance("RSA")

        val keySpec = RSAPrivateKeySpec(Base64.getEncoder().encode(key.encodeToByteArray()))

        val encryptionKey = keyFactory.generatePrivate(keySpec)

        cipher.init(Cipher.ENCRYPT_MODE,encryptionKey)

        val e = cipher.doFinal(value.encodeToByteArray())

        Log.d("CRYPTOOOO", e.decodeToString())
    }

    fun decrypt() {

    }

    fun sign() {

    }
}