package com.example.passman.presentation.vault

typealias PasswordsData = MutableMap<String, String>

data class VaultData(val name: String, val publicKey: String, val passwords: PasswordsData)

val defaultVaultData = listOf<VaultData>();

