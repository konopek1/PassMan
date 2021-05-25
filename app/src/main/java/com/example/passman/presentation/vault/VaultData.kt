package com.example.passman.presentation.vault

typealias PasswordsData = MutableMap<String, String>

data class VaultData(val name: String, val publicKey: String, val passwords: PasswordsData)

val defaultVaultData = listOf(
    VaultData(
        "Vault 1", "MIIHOTCCBiGgAwIBAgISA4srJU6bpT7xpINN6bbGO2", mutableMapOf(
            "name 1" to "password1",
            "name 2" to "password2"
        )
    ), VaultData(
        "Vault 2", "MIIHOTCCBiGgAwIBAgISA4srJU6bpT7xpINN6bbGO2", mutableMapOf(
            "name 1" to "password1",
            "name 2" to "password2"
        )
    ), VaultData(
        "Vault 3", "MIIHOTCCBiGgAwIBAgISA4srJU6bpT7xpINN6bbGO2", mutableMapOf(
            "name 1" to "password1",
            "name 2" to "password2"
        )
    )
);

