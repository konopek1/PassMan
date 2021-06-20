package com.example.passman.Interactors

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import com.example.passman.domain.*
import com.example.passman.presentation.vault.VaultData
import kotlinx.serialization.*
import kotlinx.serialization.json.*

import com.android.volley.toolbox.StringRequest
import com.example.passman.api.ApiInterface
import com.example.passman.api.ApiVaultGetResponse
import com.example.passman.api.SuccessResponse
import com.example.passman.presentation.vault.defaultVaultData
import java.security.PublicKey

class VaultViewModel(activity: Activity) : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    var shareVaultQrCode by mutableStateOf<ImageBitmap?>(null)

    private val encryptedStorage = VaultKeysStorage(activity)

    private val api = ApiInterface(encryptedStorage,DiskBasedCache(activity.cacheDir, 1024 * 1024))

    init {
        val pubks = encryptedStorage.getAllPublicKeys()
        // get all vaults
        for(pubk in pubks)
        {
            val privk = encryptedStorage.read(pubk)

            api.getVault(pubk) { r ->
                val passmap = r.data.keys.map {
                    it.name to RSAHelper().decryptWithPrivate(EncodedRSAKeys(pubk,
                        privk), it.value)
                }.toMutableStateMap()
                passmap.forEach { (k, v) -> Log.d("PASSWORDS", "$k = (${v.length})$v") }
                vaults = vaults + VaultData(r.data.name, pubk, passmap)
            }
        }
    }

    fun createVault(name: String) {

        val keyPair = KeyPairGenerator().generate()

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)

        api.newVault(name,keyPair.publicKey) { r->
            vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
        }
    }

    fun importVault(keyPair: EncodedRSAKeys) {
        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)

        api.getVault(keyPair.publicKey) { r ->
            val passmap = r.data.keys.map {
                it.name to RSAHelper().decryptWithPrivate(EncodedRSAKeys(keyPair.publicKey,
                    keyPair.privateKey), it.value)
            }.toMutableStateMap()
            passmap.forEach { (k, v) -> Log.d("PASSWORDS", "$k = (${v.length})$v") }
            vaults = vaults + VaultData(r.data.name,keyPair.publicKey, passmap)
        }
    }


    fun shareVault(vaultPK: String) {
        val privateKey = encryptedStorage.read(vaultPK)

        shareVaultQrCode = QrCodeGenerator().getQrCodeBitMap("$vaultPK|$privateKey",600).asImageBitmap()
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {

        val privateKeyEncoded = encryptedStorage.read(vaultPK)

        val pass = RSAHelper().encryptWithPublic(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword)

        api.addPass(name,pass,vaultPK) { r ->
            vaults.find { it.publicKey == vaultPK }
                .also{ it?.passwords?.put(name, plainPassword) }
        }
    }
}