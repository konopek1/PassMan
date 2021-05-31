package com.example.passman.domain

import java.security.Signature
import javax.crypto.Cipher


class RSAHelper {

    /**
     * Return base64 encrypted value
     */
    fun encryptWithPublic(encodedKeys: EncodedRSAKeys, value: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")

        val keys = KeyPairGenerator().decodeKeys(encodedKeys)

        cipher.init(Cipher.ENCRYPT_MODE,keys.publicKey)

        return toBase64(cipher.doFinal(value.toByteArray()))
    }

    fun decryptWithPrivate(encodedKeys: EncodedRSAKeys, value: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/NoPadding")

        val keys = KeyPairGenerator().decodeKeys(encodedKeys)

        cipher.init(Cipher.DECRYPT_MODE,keys.privateKey)

        return cipher.doFinal(fromBase64(value)).decodeToString()
    }


    /**
     * Return base64 signed message
     */
    fun sign(keys: EncodedRSAKeys, value: String): String {
        val keys = KeyPairGenerator().decodeKeys(keys)


        val signature = Signature.getInstance("SHA512withRSA"). run {
            initSign(keys.privateKey)
            update(value.toByteArray())
            sign()
        }

        return toBase64(signature)
    }

    /**
     * Return base64 signed message
     */
    fun verify(keys: EncodedRSAKeys, value: String, sig: String): Boolean {
        val keys = KeyPairGenerator().decodeKeys(keys)

        val valid = Signature.getInstance("SHA512withRSA"). run {
            initVerify(keys.publicKey)
            update(value.toByteArray())
            verify(fromBase64(sig))
        }

        return valid
    }


}