package com.example.passman.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.math.BigInteger
import java.security.Key
import java.security.KeyStore
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class AESCipher(val activity: Activity) {

    private val TRANSFORMATION = "AES/GCM/NoPadding"

    private val metadataSharePreferences: SharedPreferences by lazy {
        activity.getSharedPreferences("xs", Context.MODE_PRIVATE)
    }

    fun encrypt(plainPassword: String, secretKey: Key): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedPassword = toBase64(cipher.doFinal(plainPassword.encodeToByteArray()))

        val passwordHash = sha512(encryptedPassword)
        setMetadata(passwordHash, Base64.getEncoder().encodeToString(cipher.iv))

        return encryptedPassword
    }

    fun decrypt(encryptedPassword: String, secretKey: Key): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)

        val passwordHash = sha512(encryptedPassword)

        val iv = getMetadata(passwordHash)
            ?: throw Exception("Failed to find IV for password")

        val paramSpec = GCMParameterSpec(128, fromBase64(iv))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec)

        return cipher.doFinal(fromBase64(encryptedPassword)).decodeToString()
    }

    private fun setMetadata(alias: String, metadata: String) {
        with(metadataSharePreferences.edit()) {
            putString(alias, metadata)
            apply()
        }

    }

    private fun sha512(data: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val messageDigest = md.digest(data.toByteArray())

        val no = BigInteger(1, messageDigest)

        var hash: String = no.toString(16)

        while (hash.length < 128) {
            hash = "0$hash"
        }

        return hash
    }

    private fun getMetadata(alias: String): String? {
        return metadataSharePreferences.getString(alias, null)
    }


    private fun toBase64(byteArray: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }

    private fun fromBase64(string: String): ByteArray {
        return Base64.getDecoder().decode(string)
    }

}