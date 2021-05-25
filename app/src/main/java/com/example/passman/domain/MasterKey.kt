package com.example.passman.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.example.passman.R
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * Highly coupled with KeyStore android
 */
class MasterKey(private val activity: Activity) {

    private val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Alias is public key
     */
    private val aliases: Enumeration<String> by lazy { ks.aliases() }

    private val AESCipher by lazy { AESCipher(activity) }

    private val masterKeyAlias by lazy { activity.getString(R.string.master_key_alias) }

    init {
        if (!doMasterKeyExist()) {
            genMasterKey()
        }
    }

    private fun getKpgForAlias(): KeyGenerator? {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val spec = KeyGenParameterSpec.Builder(masterKeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(spec)

        return keyGenerator
    }

    private fun genMasterKey() {
        val kpg = getKpgForAlias() ?: throw Exception("Failed to initialize master key generator")

        kpg.generateKey()
    }

    private fun doMasterKeyExist(): Boolean {
        return aliases.toList().contains(masterKeyAlias)
    }



    fun encrypt(plainPassword: String): String {
        val secretKeyEntry = ks.getEntry(masterKeyAlias, null) as KeyStore.SecretKeyEntry

        return AESCipher.encrypt(plainPassword,secretKeyEntry.secretKey)
    }

    fun decrypt(encryptedPassword: String): String {
        val secretKeyEntry = ks.getEntry(masterKeyAlias, null) as KeyStore.SecretKeyEntry

        return AESCipher.decrypt(encryptedPassword,secretKeyEntry.secretKey)
    }

}