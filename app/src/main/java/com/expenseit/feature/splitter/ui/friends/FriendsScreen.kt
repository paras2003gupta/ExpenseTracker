package com.expenseit.feature.splitter.ui.friends

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseit.feature.splitter.ui.components.MemberAvatar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.input.KeyboardType
import android.content.Intent
import android.provider.ContactsContract
import android.net.Uri
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel = viewModel(
        factory = FriendsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.friends.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No friends added yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add friends to split expenses with them.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Friend")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.friends, key = { it.id }) { friend ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MemberAvatar(name = friend.name, colorIndex = friend.avatarColorIndex)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = friend.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (friend.phone.isNotEmpty()) {
                                            Text(
                                                text = friend.phone,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Row {
                                    IconButton(onClick = { viewModel.setEditingFriend(friend) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteFriend(friend.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Friend Dialog
    if (state.showAddDialog) {
        val context = LocalContext.current
        var name by remember { mutableStateOf("") }
        var selectedCountry by remember { mutableStateOf(majorCountries[0]) }
        var localPhone by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        val contactPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
                val details = getPhoneContactDetails(context, contactUri)
                if (details != null) {
                    name = details.first
                    val parsed = splitPhoneNumber(details.second)
                    selectedCountry = parsed.first
                    localPhone = parsed.second
                }
            }
        }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddDialog() },
            title = { Text("Add Friend") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Friend Details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                contactPickerLauncher.launch(intent)
                            }
                        ) {
                            Text("Import Contact")
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.35f)) {
                            OutlinedTextField(
                                value = "${selectedCountry.flag} ${selectedCountry.code}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Code") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                majorCountries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text("${country.flag} ${country.country} (${country.code})") },
                                        onClick = {
                                            selectedCountry = country
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = localPhone,
                            onValueChange = { localPhone = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("e.g. 9876543210") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(0.65f),
                            singleLine = true
                        )
                    }
                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.trim().isEmpty()) {
                            errorMsg = "Name cannot be empty."
                        } else {
                            val cleanLocal = localPhone.trim()
                                .removePrefix("+")
                                .removePrefix(selectedCountry.code.removePrefix("+"))
                            val finalPhone = if (cleanLocal.isNotEmpty()) selectedCountry.code + cleanLocal else ""
                            viewModel.addFriend(name, finalPhone)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Friend Dialog
    state.editingFriend?.let { friend ->
        var name by remember { mutableStateOf(friend.name) }
        val parsedPhone = remember(friend.phone) { splitPhoneNumber(friend.phone) }
        var selectedCountry by remember { mutableStateOf(parsedPhone.first) }
        var localPhone by remember { mutableStateOf(parsedPhone.second) }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.clearEditingFriend() },
            title = { Text("Edit Friend") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.35f)) {
                            OutlinedTextField(
                                value = "${selectedCountry.flag} ${selectedCountry.code}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Code") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                majorCountries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text("${country.flag} ${country.country} (${country.code})") },
                                        onClick = {
                                            selectedCountry = country
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = localPhone,
                            onValueChange = { localPhone = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(0.65f),
                            singleLine = true
                        )
                    }
                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.trim().isEmpty()) {
                            errorMsg = "Name cannot be empty."
                        } else {
                            val cleanLocal = localPhone.trim()
                                .removePrefix("+")
                                .removePrefix(selectedCountry.code.removePrefix("+"))
                            val finalPhone = if (cleanLocal.isNotEmpty()) selectedCountry.code + cleanLocal else ""
                            viewModel.updateFriend(friend, name, finalPhone)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearEditingFriend() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class CountryCode(val country: String, val code: String, val flag: String)

val majorCountries = listOf(
    CountryCode("India", "+91", "🇮🇳"),
    CountryCode("United States", "+1", "🇺🇸"),
    CountryCode("United Kingdom", "+44", "🇬🇧"),
    CountryCode("Canada", "+1", "🇨🇦"),
    CountryCode("Australia", "+61", "🇦🇺"),
    CountryCode("United Arab Emirates", "+971", "🇦🇪"),
    CountryCode("Singapore", "+65", "🇸🇬"),
    CountryCode("Germany", "+49", "🇩🇪"),
    CountryCode("France", "+33", "🇫🇷"),
    CountryCode("Saudi Arabia", "+966", "🇸🇦")
)

fun splitPhoneNumber(fullPhone: String): Pair<CountryCode, String> {
    val cleanPhone = fullPhone.trim()
    for (cc in majorCountries) {
        if (cleanPhone.startsWith(cc.code)) {
            return Pair(cc, cleanPhone.substring(cc.code.length))
        }
    }
    return Pair(majorCountries[0], cleanPhone) // Default to India (+91)
}

fun getPhoneContactDetails(context: android.content.Context, contactUri: Uri): Pair<String, String>? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val name = if (nameIndex >= 0) it.getString(nameIndex) else ""
            val number = if (numberIndex >= 0) it.getString(numberIndex) else ""
            return Pair(name, number)
        }
    }
    return null
}
