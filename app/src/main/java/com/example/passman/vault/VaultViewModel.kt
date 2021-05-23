package com.example.passman.vault

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VaultViewModel : ViewModel() {

    var vaults by mutableStateOf(listOf<VaultData>())
        private set

    init {
        vaults = vaults + defaultVaultData;
    }

    fun createVault(name: String) {
        vaults = vaults + listOf(defaultVaultData[0])
    }

    fun removeVault(vaultPK: String) {

    }

    fun shareVault(vaultPK: String) {

    }

    fun createNewPassword(name: String, password: String, vaultPK: String) {
        vaults = vaults + defaultVaultData;
    }

}