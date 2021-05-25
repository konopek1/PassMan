package com.example.passman.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.example.passman.R

class EncryptedStorage(val activity: Activity) {

    private val masterKey by lazy { MasterKey(activity) }

    private val sharedPreferencesKey by lazy { activity.getString(R.string.keys) }

    private val sharedPreferences: SharedPreferences by lazy {
        activity.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
    }

    fun write(key: String, value: String) {
        val password = masterKey.encrypt(value)

        with(sharedPreferences.edit()) {
            putString(key,password)
            apply()
        }
    }

    fun read(key: String): String {
        val encryptedValue = sharedPreferences.getString(key, null) ?: throw Exception("No key [$key] in storage")

        return masterKey.decrypt(encryptedValue)
    }
}