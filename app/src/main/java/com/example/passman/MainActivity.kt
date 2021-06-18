package com.example.passman

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.passman.Interactors.VaultViewModel
import com.example.passman.domain.EncodedRSAKeys
import com.example.passman.presentation.vault.VaultData
import com.example.passman.presentation.vault.passwords.Passwords
import com.example.passman.ui.theme.PassManTheme
import com.example.passman.ui.theme.Shapes
import com.example.passman.ui.theme.Typography


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vaultViewModel = VaultViewModel(this)

        setContent {
            App(
                vaultViewModel.vaults,
                vaultViewModel::addPassword,
                vaultViewModel::createVault,
                vaultViewModel::shareVault,
                vaultViewModel.shareVaultQrCode,
                vaultViewModel::importVault
            )
        }
    }

}

@Composable
fun App(
    vaultData: List<VaultData>,
    onPasswordAdd: (String, String, String) -> Unit,
    onVaultCreate: (String) -> Unit,
    onShareVault: (String) -> Unit,
    shareVaultQrCode: ImageBitmap?,
    onImportVault: (EncodedRSAKeys) -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme()

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
            TopBar(darkThemeState, updateDarkThemeState, onImportVault)
            VaultList(vaultData, onPasswordAdd, onVaultCreate, onShareVault, shareVaultQrCode)
        }
    }
}

@Composable
fun TopBar(darkTheme: Boolean, updateDarkThemeState: () -> Unit, onImportVault: (EncodedRSAKeys) -> Unit) {
    val launcher = rememberLauncherForActivityResult(contract = QrCodeScannerContract()) {
        onImportVault(it)
        Log.d("Vault import", "imported vault: $it")
    }

    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("PassMan") },
        navigationIcon = {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(onClick = { launcher.launch(null); showMenu = false; }) {
                    Text("Import vault")
                }
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = {
            Row() {
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
        }
    )
}


// TODO: Zmienijszyc buttony
@Composable
fun Vault(
    vaultData: VaultData,
    onPasswordAdd: (String, String, String) -> Unit,
    onShareVault: (String) -> Unit,
    shareVaultQrCode: ImageBitmap?,
) {
    val passwordsVisible = remember { mutableStateOf(false) }
    val openQrDialog = remember { mutableStateOf(false) }



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
                "Public Key",
                style = Typography.caption,
                modifier = Modifier.padding(start = 1.dp)
            )

            if (passwordsVisible.value) Passwords(vaultData, onPasswordAdd)

            Spacer(modifier = Modifier.height(10.dp))

            VaultButtons(passwordsVisible.value, {
                passwordsVisible.value = !passwordsVisible.value
            }, {
                onShareVault(vaultData.publicKey)
                openQrDialog.value = true
            })
        }
    }

    ShareVaultQrCode(shareVaultQrCode, openQrDialog)
}

@Composable
fun ShareVaultQrCode(
    shareVaultQrCode: ImageBitmap?,
    open: MutableState<Boolean>,
) {
    if (open.value && shareVaultQrCode != null) {
        AlertDialog(
            onDismissRequest = { open.value = !open.value },
            title = {
                Text("Vault private key",
                    style = Typography.h5,
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            },
            text = {
                Image(
                    bitmap = shareVaultQrCode,
                    contentDescription = "Qr code",
                )
            },
            buttons = {}
        )


    }
}

@Composable
fun VaultButtons(passwordsVisible: Boolean, showPasswords: () -> Unit, onShareVault: () -> Unit) {
    Row() {
        Button(
            onClick = onShareVault,
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
    onVaultCreate: (String) -> Unit,
    onShareVault: (String) -> Unit,
    shareVaultQrCode: ImageBitmap?,
) {

    val (isNewVaultDialogOpen, setOpen) = remember { mutableStateOf(false) }

    AddVaultInputDialog(onVaultCreate, setOpen, isNewVaultDialogOpen)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        for (vault: VaultData in vaults) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Vault(vault, onPasswordAdd, onShareVault, shareVaultQrCode)
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
                    setName("")
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

