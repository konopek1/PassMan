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
import com.example.passman.vault.VaultData
import com.example.passman.vault.VaultViewModel


class MainActivity : ComponentActivity() {

    val vaultViewModel by viewModels<VaultViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(vaultViewModel)
        }
    }
}

@Composable
fun App(vaultViewModel: VaultViewModel, darkTheme: Boolean = isSystemInDarkTheme()) {
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
            VaultList(vaultViewModel)
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
        modifier = Modifier
            .fillMaxWidth(0.85f)
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
fun VaultList(vaultViewModel: VaultViewModel) {
    Spacer(modifier = Modifier.height(10.dp))

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        for (vault: VaultData in vaultViewModel.vaults) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Vault(vault, vaultViewModel::createNewPassword)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun Passwords(
    vaultData: VaultData,
    onPasswordAdd: (name: String, password: String, vaultPK: String) -> Unit,
) {

    val (isPasswordInputOpen, setOpen) = remember { mutableStateOf(false) }

    NewPasswordDialog(
        isOpen = isPasswordInputOpen,
        setOpen = setOpen,
        vaultPK = vaultData.publicKey,
        onPasswordAdd = onPasswordAdd
    )

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
fun NewPasswordDialog(
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

