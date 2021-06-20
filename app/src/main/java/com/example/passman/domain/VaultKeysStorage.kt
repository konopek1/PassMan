package com.example.passman.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.example.passman.R

class VaultKeysStorage(val activity: Activity) {

    public val context = activity

    private val masterKey by lazy { MasterKey(activity) }

    private val sharedPreferencesKey by lazy { activity.getString(R.string.vault_keys) }

    private val sharedPreferences: SharedPreferences by lazy {
        activity.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
    }

    fun write(publicKey: String, privateKey: String) {
        val password = masterKey.encrypt(privateKey)

        with(sharedPreferences.edit()) {
            putString(publicKey,password)
            apply()
        }
    }

    fun read(publicKey: String): String {
        val encryptedValue = sharedPreferences.getString(publicKey, null) ?: throw Exception("No key [$publicKey] in storage")

        return masterKey.decrypt(encryptedValue)
    }

    fun getAllPublicKeys(): MutableSet<String> {
        return  sharedPreferences.all.keys
    }
}