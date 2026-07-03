package com.expenseit.feature.splitter.ui.groups

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.expenseit.feature.splitter.ui.friends.CountryCode
import com.expenseit.feature.splitter.ui.friends.majorCountries
import com.expenseit.feature.splitter.ui.friends.splitPhoneNumber
import com.expenseit.feature.splitter.ui.friends.getPhoneContactDetails
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseit.core.util.MoneyFormatter
import com.expenseit.feature.splitter.ui.components.MemberAvatar
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = viewModel(
        factory = GroupsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Expenses", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleCreateSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.groups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No groups yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a group with friends to start splitting expenses.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.toggleCreateSheet(true) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Group")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.groups, key = { it.group.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupClick(item.group.id) },
                            shape = RoundedCornerShape(16.dp),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallSplit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = item.group.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.memberCount} members",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        val bal = item.yourBalance
                                        val text = when {
                                            bal > 0 -> "You are owed"
                                            bal < 0 -> "You owe"
                                            else -> "Settled up"
                                        }
                                        val col = when {
                                            bal > 0 -> Color(0xFF059669)
                                            bal < 0 -> Color(0xFFDC2626)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                        Text(
                                            text = text,
                                            fontSize = 11.sp,
                                            color = col,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (bal != 0L) {
                                            Text(
                                                text = MoneyFormatter.formatINR(abs(bal)),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = col
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.deleteGroup(item.group.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Group",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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

    if (state.showCreateSheet) {
        CreateGroupSheet(
            friends = state.friends,
            onDismiss = { viewModel.toggleCreateSheet(false) },
            onCreate = { name, selectedIds ->
                viewModel.createGroup(name, selectedIds)
            },
            onAddFriendQuickly = { name, phone, onAdded ->
                viewModel.addFriendQuickly(name, phone, onAdded)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupSheet(
    friends: List<com.expenseit.core.model.FriendEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit,
    onAddFriendQuickly: (String, String, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var groupName by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }
    var errorMsg by remember { mutableStateOf("") }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create split group",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                placeholder = { Text("e.g. Trip to Goa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add members (You will be added automatically)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showAddFriendDialog = true }) {
                    Text("+ Add Friend")
                }
            }

            if (friends.isEmpty()) {
                Text(
                    text = "No friends added. Please add friends first in the Friends tab to select them.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(friends) { friend ->
                        val isSelected = selectedIds.contains(friend.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedIds.remove(friend.id)
                                    else selectedIds.add(friend.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked == true) selectedIds.add(friend.id)
                                    else selectedIds.remove(friend.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MemberAvatar(name = friend.name, colorIndex = friend.avatarColorIndex, size = 32.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(friend.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (groupName.trim().isEmpty()) {
                            errorMsg = "Group name is required."
                        } else {
                            onCreate(groupName, selectedIds.toList())
                        }
                    }
                ) {
                    Text("Create")
                }
            }
        }
    }

    if (showAddFriendDialog) {
        val context = LocalContext.current
        var friendName by remember { mutableStateOf("") }
        var selectedCountry by remember { mutableStateOf(majorCountries[0]) }
        var localPhone by remember { mutableStateOf("") }
        var dialogError by remember { mutableStateOf("") }

        val contactPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
                val details = getPhoneContactDetails(context, contactUri)
                if (details != null) {
                    friendName = details.first
                    val parsed = splitPhoneNumber(details.second)
                    selectedCountry = parsed.first
                    localPhone = parsed.second
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Quick Add Friend") },
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
                        value = friendName,
                        onValueChange = { friendName = it },
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
                            modifier = Modifier.weight(0.65f),
                            singleLine = true
                        )
                    }

                    if (dialogError.isNotEmpty()) {
                        Text(dialogError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendName.trim().isEmpty()) {
                            dialogError = "Name is required."
                        } else {
                            val cleanLocal = localPhone.trim()
                                .removePrefix("+")
                                .removePrefix(selectedCountry.code.removePrefix("+"))
                            val finalPhone = if (cleanLocal.isNotEmpty()) selectedCountry.code + cleanLocal else ""
                            onAddFriendQuickly(friendName, finalPhone) { newId ->
                                selectedIds.add(newId)
                                showAddFriendDialog = false
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
