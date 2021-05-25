package com.example.passman.Interactors

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.passman.domain.AESCipher
import com.example.passman.domain.EncryptedStorage
import com.example.passman.domain.KeyPairGenerator
import com.example.passman.domain.RSAHelper
import com.example.passman.presentation.vault.PasswordsData
import com.example.passman.presentation.vault.VaultData
import com.example.passman.presentation.vault.defaultVaultData
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

// TODO: dopisac klase do podpisywania

class VaultViewModel(val activity: Activity) : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    private val encryptedStorage = EncryptedStorage(activity)

    init {
        // TODO: api call -> on start fetch tha from api
        vaults = vaults + defaultVaultData
    }

    fun createVault(name: String) {
        val keyPair = KeyPairGenerator().generate()

        Log.d("SHARE START PK: ",keyPair.publicKey)

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)
        // TODO: api call -> create new vault && vaults = vaults + new vault
        // but don't touch private key

        // temp should be invoked in response to api call
        vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
    }


    fun shareVault(vaultPK: String) {
        Log.d("SHARE START PK: ",vaultPK)

        val privateKey = encryptedStorage.read(vaultPK)

        Log.d("SHARE PRIVATE KEY: ", privateKey)
        // TODO: display QR CODE
        // TODO: scan qr code
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {
        // TODO: api call -> add password to vault && vault[name] = passwords + new password

        val privateKeyEncoded = encryptedStorage.read(vaultPK)

        RSAHelper().encrypt(privateKeyEncoded,plainPassword)

        // temp should be invoked in response to api call
        vaults.find { it.publicKey == vaultPK }.also { it?.passwords?.put(name,plainPassword) }
    }


}