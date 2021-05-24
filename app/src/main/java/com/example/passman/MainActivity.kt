package com.example.passman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.passman.ui.theme.PassManTheme
import com.example.passman.ui.theme.Shapes
import com.example.passman.ui.theme.Typography
import com.example.passman.presentation.vault.VaultData
import com.example.passman.presentation.vault.VaultViewModel
import com.example.passman.presentation.vault.passwords.Passwords


class MainActivity : ComponentActivity() {

    val vaultViewModel by viewModels<VaultViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                vaultViewModel.vaults,
                vaultViewModel::addPassword,
                vaultViewModel::createVault
            )
        }
    }
}

@Composable
fun App(
    vaultData: List<VaultData>,
    onPasswordAdd: (String, String, String) -> Unit,
    onVaultCreate: (String) -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    var darkThemeState by remember { mutableStateOf(darkTheme) }
    val updateDarkThemeState = { darkThemeState = !darkThemeState }

    PassManTheme(darkThemeState) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(if (!darkThemeState) Color.White else Color.Black)
        ) {
            TopBar(darkThemeState, updateDarkThemeState)
            VaultList(vaultData, onPasswordAdd, onVaultCreate)
        }
    }
}

@Composable
fun TopBar(darkTheme: Boolean, updateDarkThemeState: () -> Unit) {
    TopAppBar(
        title = { Text("PassMan") },
        navigationIcon = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { updateDarkThemeState() }) {
                if (darkTheme) {
                    Icon(
                        painter = painterResource(id = R.drawable.dark_theme),
                        contentDescription = "Dark theme"
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_wb_sunny_24),
                        contentDescription = "Light theme"
                    )
                }
            }
        }
    )
}


// TODO: Zmienijszyc buttony
@Composable
fun Vault(
    vaultData: VaultData,
    onPasswordAdd: (String, String, String) -> Unit,
) {
    val passwordsVisible = remember { mutableStateOf(false); }

    Card(
        Modifier.fillMaxWidth(0.85f)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                vaultData.name,
                style = Typography.h4,
            )

            Text(
                "Public key",
                style = Typography.caption,
                modifier = Modifier.padding(start = 1.dp)
            )

            if (passwordsVisible.value) Passwords(vaultData, onPasswordAdd)

            Spacer(modifier = Modifier.height(10.dp))

            VaultButtons(passwordsVisible.value) {
                passwordsVisible.value = !passwordsVisible.value
            }
        }
    }

}

@Composable
fun VaultButtons(passwordsVisible: Boolean, showPasswords: () -> Unit) {
    Row() {
        Button(
            onClick = {/* doSomething() */ },
            modifier = Modifier.defaultMinSize(0.dp, 0.dp),
            shape = Shapes.small
        ) {
            Icon(Icons.Outlined.Share, contentDescription = "Share vault")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Share", style = Typography.button)
        }
        Spacer(modifier = Modifier.size(10.dp))
        Button(
            onClick = showPasswords,
            modifier = Modifier.defaultMinSize(0.dp, 0.dp),
            shape = Shapes.small
        ) {
            if (passwordsVisible) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = "Close vault"
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Close", style = Typography.button)
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.lock_open),
                    contentDescription = "Open vault"
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Open", style = Typography.button)
            }
        }
    }

}


@Composable
fun VaultList(
    vaults: List<VaultData>,
    onPasswordAdd: (String, String, String) -> Unit,
    onVaultCreate: (String) -> Unit
    ) {

    val (isNewVaultDialogOpen, setOpen) = remember { mutableStateOf(false) }

    AddVaultInputDialog(onVaultCreate, setOpen, isNewVaultDialogOpen)

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        for (vault: VaultData in vaults) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Vault(vault, onPasswordAdd)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        AddVaultButton(setOpen)

        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
fun AddVaultInputDialog(
    onVaultAdd: (name: String) -> Unit,
    setOpen: (b: Boolean) -> Unit,
    isOpen: Boolean,
) {
    val (name, setName) = remember { mutableStateOf("") }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = { setOpen(false) },
            confirmButton = {
                Button(onClick = {
                    onVaultAdd(name)
                    setOpen(false)
                }) {
                    Text("Create")
                }
            },
            title = {
                Text("New vault",
                    style = Typography.h6,
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            },
            text = {
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = name,
                    onValueChange = { setName(it) }, label = { Text("Name") }
                )
            }
        )
    }
}

@Composable
fun AddVaultButton(
    setOpen: (b: Boolean) -> Unit,
) {
    Button(
        onClick = {
            setOpen(true);
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add password to vault")
            Text("New vault")
        }
    }
}


@Composable
fun Password(name: String, decryptedPassword: String) {
    val isShown = remember { mutableStateOf(false); }

    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = Typography.button, modifier = Modifier.weight(1f))
        TextButton(onClick = { isShown.value = !isShown.value }) {
            if (isShown.value) Text(decryptedPassword) else Text("******");
        }
    }
}

