package com.example.passman.Interactors

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.example.passman.domain.*
import com.example.passman.presentation.vault.VaultData
import com.example.passman.presentation.vault.defaultVaultData
import java.security.PublicKey

class VaultViewModel(activity: Activity) : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    var shareVaultQrCode by mutableStateOf<ImageBitmap?>(null)

    private val encryptedStorage = VaultKeysStorage(activity)

    init {
        // TODO: api call -> on start fetch tha from api
        vaults = vaults + defaultVaultData
    }

    fun createVault(name: String) {
        val keyPair = KeyPairGenerator().generate()

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)
        // TODO: api call -> create new vault && vaults = vaults + new vault
        // but don't touch private key

        // temp should be invoked in response to api call
        vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
    }

    fun importVault(keyPair: EncodedRSAKeys) {
        encryptedStorage.write(keyPair.publicKey,keyPair.publicKey)

        // TODO: Radek tutaj dodaj żeby pobierał hasła do tego sejfu nowego
    }


    fun shareVault(vaultPK: String) {
        val privateKey = encryptedStorage.read(vaultPK)

        shareVaultQrCode = QrCodeGenerator().getQrCodeBitMap("$vaultPK|$privateKey",600).asImageBitmap()
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {

        // temp should be invoked in response to api call
        vaults.find { it.publicKey == vaultPK }.also { it?.passwords?.put(name,plainPassword) }
    }


}