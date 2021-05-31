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





@Serializable
data class ApiPass(val name: String, val value: String)

@Serializable
data class ApiVaultGetData(val keys:List<ApiPass>, val name:String)

@Serializable
data class ApiReq(val data:Map<String,String>, val sign:String)


@Serializable
data class SuccessResponse(val status:String)
@Serializable
data class ApiVaultGetResponse(val data:ApiVaultGetData,val status :String)





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

        val pubks = encryptedStorage.getAllPublicKeys()

        for(pubk in pubks)
        {
            val privk = encryptedStorage.read(pubk)

            val req = makeReq(mapOf(
                "public_key" to pubk,
            ),pubk)

            invokeReq(baseUrl+endpoint, req,{ response ->
                val r = Json.decodeFromString<ApiVaultGetResponse>(response.toString())
                if(r.status == "success")
                {
                    vaults = vaults + VaultData(r.data.name,pubk,
                        r.data.keys.map { it.name to RSAHelper().decryptWithPrivate(EncodedRSAKeys(pubk,privk),it.value) }.toMutableStateMap())
                }
                else {
                    // TODO: Handle error
                    Log.d("ERROR MAKING REQ: ", r.status)
                }
            } )
        }
    }

    fun createVault(name: String) {
        val endpoint = "/vault/new"
        val keyPair = KeyPairGenerator().generate()

        Log.d("SHARE START PK: ",keyPair.publicKey)

        encryptedStorage.write(keyPair.publicKey,keyPair.privateKey)

        val req = makeReq(mapOf(
            "public_key" to keyPair.publicKey,
            "name" to name
        ),keyPair.publicKey)

        invokeReq(baseUrl+endpoint,req,{ response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if(r.status == "success")
            {
                vaults = vaults + listOf(VaultData(name, keyPair.publicKey, mutableMapOf()))
            }
            else
            {
                //todo handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        })


    }


    fun shareVault(vaultPK: String) {
        val privateKey = encryptedStorage.read(vaultPK)

        shareVaultQrCode = QrCodeGenerator().getQrCodeBitMap(privateKey,600).asImageBitmap()
    }

    fun addPassword(name: String, plainPassword: String, vaultPK: String) {
        // TODO: api call -> add password to vault && vault[name] = passwords + new password
        val endpoint = "/pass/add"
        val privateKeyEncoded = encryptedStorage.read(vaultPK)

        val pass = RSAHelper().encryptWithPublic(EncodedRSAKeys(vaultPK,privateKeyEncoded),plainPassword)

        val req = makeReq(mapOf(
            "public_key" to vaultPK,
            "name" to name,
            "value" to pass
        ),vaultPK)

        invokeReq(baseUrl+endpoint, req, { response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if (r.status == "success") {
                vaults.find { it.publicKey == vaultPK }.also { it?.passwords?.put(name,plainPassword) }
            } else {
                //todo handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        })
        // temp should be invoked in response to api call

    }

    fun invokeReq(url :String, data:String, onResponse : Response.Listener<String>){

        Log.d("REQUEST", "Sending request to " + url + "\n with:\n" +data);
        val req: StringRequest = object : StringRequest(Method.POST, url, onResponse,
            { error ->
                //todo show error message
                if(error.networkResponse != null && error.networkResponse.data != null )
                {
                    val resp = Json.decodeFromString<SuccessResponse>(error.networkResponse.data.decodeToString())
                    Log.d("REQUEST RESPONSE ERROR", resp.status)
                }
                else
                {
                    Log.d("REQUEST RESPONSE ERROR ", "ERRMSG:"+error.message)
                }

            }
        ) {
            override fun getBody(): ByteArray {
                return data.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Content-Type" to "application/json"
                )
            }
        }

        requestQueue.add(req)
    }

    fun makeReq(data: Map<String, String>, vaultPK: String): String {
        val privateKey = encryptedStorage.read(vaultPK)
        val signature =  RSAHelper().sign(EncodedRSAKeys(vaultPK, privateKey), Json.encodeToString(data));

        Log.d("SIGNED: ", Json.encodeToString(data) + " AS " + signature)
        return Json.encodeToString(ApiReq(data, signature))
    }
}