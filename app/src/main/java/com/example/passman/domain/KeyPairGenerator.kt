package com.example.passman.domain

import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.*
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

/**
 * Base64 encoded
 */
data class EncodedRSAKeys(val publicKey: String, val privateKey: String)

data class DecodedRSAKeys(val publicKey: RSAPublicKey, val privateKey: RSAPrivateKey)

class KeyPairGenerator {

    val SEPARATOR = "#"

    fun generate(): EncodedRSAKeys {

        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)

        kpg.initialize(2048)
        val keyPair = kpg.generateKeyPair()

        val privateKey = (keyPair.private as RSAPrivateKey).privateExponent.toByteArray()
        val publicKeyModulus = (keyPair.public as RSAPublicKey).modulus.toByteArray()
        val publicKeyExponent = (keyPair.public as RSAPublicKey).publicExponent.toByteArray()

        val encodedPrivateKey = toBase64(privateKey)
        val encodedPublicKeyModulus = toBase64(publicKeyModulus)
        val encodedPublicKeyExponent = toBase64(publicKeyExponent)


        return EncodedRSAKeys("$encodedPublicKeyModulus$SEPARATOR$encodedPublicKeyExponent", encodedPrivateKey)
    }

    fun decodeKeys(encodedKeysEncoded: EncodedRSAKeys): DecodedRSAKeys {
        val encodedPrivateKey = encodedKeysEncoded.privateKey
        val encodedPublicKey = encodedKeysEncoded.publicKey.split(SEPARATOR)

        val pModulus = encodedPublicKey[0]
        val pExponent = encodedPublicKey[1]

        val privateExponenet = BigInteger(1,fromBase64(encodedPrivateKey))
        val modulus = BigInteger(1,fromBase64(pModulus))
        val exponent = BigInteger(1,fromBase64(pExponent))


        val pubSpec = RSAPublicKeySpec(modulus,exponent)

        val privateSpec = RSAPrivateKeySpec(modulus,privateExponenet)

        val kf = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)

        val privateKey = kf.generatePrivate(privateSpec) as RSAPrivateKey
        val publicKey = kf.generatePublic(pubSpec) as RSAPublicKey

        return DecodedRSAKeys(publicKey, privateKey)
    }

}

