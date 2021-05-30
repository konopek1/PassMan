package com.example.passman.Interactors

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.example.passman.domain.*
import com.example.passman.presentation.vault.VaultData
import com.example.passman.presentation.vault.defaultVaultData


class VaultViewModel(activity: Activity) : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    var shareVaultQrCode by mutableStateOf<ImageBitmap?>(null)

    private val encryptedStorage = VaultKeysStorage(activity)

    // Instantiate the cache
    val cache = DiskBasedCache(activity.cacheDir, 1024 * 1024) // 1MB cap

    // Set up the network to use HttpURLConnection as the HTTP client.
    val network = BasicNetwork(HurlStack())

    // Instantiate the RequestQueue with the cache and network. Start the queue.
    val requestQueue = RequestQueue(cache, network).apply {
        start()
    }

    //todo: change to https
    val baseUrl = "http://192.168.0.12:3000"


    init {
        val endpoint = "/vault/get"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, baseUrl+endpoint, null,
            { response ->
                response.toString()
            },
            { error ->
                // TODO: Handle error
            }
        )


// Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)
        vaults.get(0).

        vaults = vaults + defaultVaultData
    }

    fun createVault(name: String) {
        val endpoint = "/vault/new"
        val keyPair = KeyPairGenerator().generate()

        Log.d("SHARE START PK: ",keyPair.publicKey)

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, baseUrl+endpoint, null,
            { response ->
                response.toString()
            },
            { error ->
                // TODO: Handle error
            }
        )

        // temp should be invoked in response to api call
        vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
    }


    fun shareVault(vaultPK: String) {
        val privateKey = encryptedStorage.read(vaultPK)

        shareVaultQrCode = QrCodeGenerator().getQrCodeBitMap(privateKey,600).asImageBitmap()
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {
        // TODO: api call -> add password to vault && vault[name] = passwords + new password

        val privateKeyEncoded = encryptedStorage.read(vaultPK)

        val pass = RSAHelper().encryptWithPrivate(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword)

        Log.d("asdasdaasdasdasdasd ((((((((((((((((((((S",pass)


        val e = RSAHelper().decryptWithPublic(EncodedRSAKeys(vaultPK,privateKeyEncoded),pass)

        Log.d("asdasdaasdasdasdasd ((((((((((((((((((((S",e)


        val sig = RSAHelper().sign(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword)


        val isValid = RSAHelper().verify(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword, sig)

        Log.d("asdasdaasdasdasdasd ((((((((((((((((((((S",isValid.toString())

        // temp should be invoked in response to api call
        vaults.find { it.publicKey == vaultPK }.also { it?.passwords?.put(name,plainPassword) }
    }


}