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

class VaultViewModel(activity: Activity) : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    var shareVaultQrCode by mutableStateOf<ImageBitmap?>(null)

    private val encryptedStorage = VaultKeysStorage(activity)

    private val api = ApiInterface(encryptedStorage,DiskBasedCache(activity.cacheDir, 1024 * 1024))

    init {
        val endpoint = "/vault/get"

        val pubks = encryptedStorage.getAllPublicKeys()

        for(pubk in pubks)
        {
            val privk = encryptedStorage.read(pubk)

            api.getVault(pubk) { response ->
                val r = Json.decodeFromString<ApiVaultGetResponse>(response.toString())
                if (r.status == "success") {
                    val passmap = r.data.keys.map {
                        it.name to RSAHelper().decryptWithPrivate(EncodedRSAKeys(pubk,
                            privk), it.value)
                    }.toMutableStateMap()
                    passmap.forEach { k, v -> Log.d("PASSWORDS", "$k = (${v.length})$v") }
                    vaults = vaults + VaultData(r.data.name, pubk, passmap)
                } else {
                    // TODO: Handle error
                    Log.d("ERROR MAKING REQ: ", r.status)
                }
            }
        }
    }

    fun createVault(name: String) {

        val keyPair = KeyPairGenerator().generate()

        Log.d("SHARE START PK: ",keyPair.publicKey)

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)

        api.newVault(name,keyPair.publicKey) { response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if (r.status == "success") {
                vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
            } else {
                //todo handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        }
    }

    fun shareVault(vaultPK: String) {
        val privateKey = encryptedStorage.read(vaultPK)

        shareVaultQrCode = QrCodeGenerator().getQrCodeBitMap(privateKey,600).asImageBitmap()
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {

        val privateKeyEncoded = encryptedStorage.read(vaultPK)

        val pass = RSAHelper().encryptWithPublic(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword)

        api.addPass(name,pass,vaultPK) { response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if (r.status == "success") {
                vaults.find { it.publicKey == vaultPK }
                    .also { it?.passwords?.put(name, plainPassword) }
            } else {
                //todo handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        }
    }
}