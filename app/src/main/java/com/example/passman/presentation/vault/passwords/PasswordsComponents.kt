package com.example.passman.presentation.vault.passwords

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.passman.Password
import com.example.passman.presentation.vault.VaultData
import com.example.passman.ui.theme.Typography


@Composable
fun Passwords(
    vaultData: VaultData,
    onPasswordAdd: (name: String, password: String, vaultPK: String) -> Unit,
) {
    val (isPasswordInputOpen, setOpen) = remember { mutableStateOf(false) }

    NewPasswordDialogInput(isPasswordInputOpen, setOpen, vaultData.publicKey, onPasswordAdd)

    Column() {
        for ((name, password) in vaultData.passwords) {
            Password(name, password)
            Divider()
        }
        IconButton(
            onClick = {
                setOpen(true)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Filled.AddCircle, contentDescription = "Add password to vault")
        }
    }
}

@Composable
fun NewPasswordDialogInput(
    isOpen: Boolean,
    setOpen: (b: Boolean) -> Unit,
    vaultPK: String,
    onPasswordAdd: (name: String, password: String, vaultPK: String) -> Unit,
) {

    val (name, setName) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = { setOpen(false) },
            confirmButton = {
                Button(onClick = {
                    onPasswordAdd(name, password, vaultPK)
                    setOpen(false)
                }) {
                    Text("Create")
                }
            },
            title = {
                Text("New password",
                    style = Typography.h6,
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            },
            text = {
                Column(Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                    TextField(
                        value = name,
                        onValueChange = { setName(it) }, label = { Text("Name") }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    TextField(
                        value = password,
                        onValueChange = { setPassword(it) }, label = { Text("Password") }
                    )
                }
            },
        )
    }
}