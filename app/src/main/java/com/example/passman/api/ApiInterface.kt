package com.example.passman.api

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.toMutableStateMap
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.example.passman.domain.EncodedRSAKeys
import com.example.passman.domain.RSAHelper
import com.example.passman.domain.VaultKeysStorage
import com.example.passman.presentation.vault.VaultData
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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


class ApiInterface(
    estore: VaultKeysStorage,// Instantiate the cache
    private val cache: DiskBasedCache// 1MB cap
) {

    // Set up the network to use HttpURLConnection as the HTTP client.
    val network = BasicNetwork(HurlStack())

    // Instantiate the RequestQueue with the cache and network. Start the queue.
    private val requestQueue : RequestQueue = RequestQueue(cache, network).apply {
        start()
    }
    private val encryptedStorage : VaultKeysStorage = estore;

    val baseUrl = "http://172.18.120.215:3000"

    fun getVault(pubk : String, onResponse : (ApiVaultGetResponse) -> Unit) {
        val endpoint = "/vault/get"

        val req = makeReq(mapOf(
            "public_key" to pubk,
        ),pubk)

        invokeReq(baseUrl+endpoint, req) { response ->
            val r = Json.decodeFromString<ApiVaultGetResponse>(response.toString())

            if (r.status == "success") {
                onResponse(r)
            } else {
                // TODO: Handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        }

    }

    fun newVault(name : String, pubk: String, onResponse : (SuccessResponse) -> Unit) {
        val endpoint = "/vault/new"

        val req = makeReq(mapOf(
            "public_key" to pubk,
            "name" to name
        ),pubk)

        invokeReq(baseUrl+endpoint,req) { response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if (r.status == "success") {
                onResponse(r)
            } else {
                // TODO: Handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        }
    }

    fun addPass(name : String, pass: String, pk: String, onResponse : (SuccessResponse) -> Unit){
        val endpoint = "/pass/add"

        val req = makeReq(mapOf(
            "public_key" to pk,
            "name" to name,
            "value" to pass
        ),pk)

        invokeReq(baseUrl+endpoint,req) { response ->
            val r = Json.decodeFromString<SuccessResponse>(response.toString())
            if (r.status == "success") {
                onResponse(r)
            } else {
                // TODO: Handle error
                Log.d("ERROR MAKING REQ: ", r.status)
            }
        }
    }

    private fun invokeReq(url :String, data:String, onResponse : Response.Listener<String>){

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

                encryptedStorage.context.runOnUiThread {
                    Toast.makeText(encryptedStorage.context, "Network error!",
                        Toast.LENGTH_LONG).show()
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

    private fun makeReq(data: Map<String, String>, vaultPK: String): String {
        val privateKey = encryptedStorage.read(vaultPK)
        val signature =  RSAHelper().sign(EncodedRSAKeys(vaultPK, privateKey), Json.encodeToString(data));

        Log.d("SIGNED: ", Json.encodeToString(data) + " AS " + signature)
        return Json.encodeToString(ApiReq(data, signature))
    }
}