package com.expenseit.feature.splitter.ui.group_detail

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenseit.core.util.DateUtils
import com.expenseit.core.util.MoneyFormatter
import com.expenseit.feature.splitter.ui.components.BalanceCard
import com.expenseit.feature.splitter.ui.components.MemberAvatar
import java.net.URLEncoder
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = viewModel(
        factory = GroupDetailViewModelFactory(LocalContext.current.applicationContext as Application, groupId)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var initialFromMemberId by remember { mutableStateOf("") }
    var initialToMemberId by remember { mutableStateOf("") }
    var initialAmount by remember { mutableStateOf(0.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.group?.name ?: "Group Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAddMember(true) }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.toggleAddExpense(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Expenses", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Balances", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Settle Up", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            when (selectedTab) {
                // TAB 0: EXPENSES
                0 -> {
                    if (state.expenses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No group expenses yet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.toggleAddExpense(true) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add expense")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.expenses, key = { it.id }) { expense ->
                                val paidByMember = state.members.find { it.id == expense.paidByMemberId }
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = expense.description,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Paid by ${paidByMember?.name ?: "Unknown"} • ${DateUtils.formatDate(expense.txnDate)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = MoneyFormatter.formatINR(expense.amountMinor),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(onClick = { viewModel.deleteExpense(expense.id) }) {
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

                // TAB 1: BALANCES
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.members) { member ->
                            val balance = state.balances[member.id] ?: 0L
                            val friendProfile = state.friendProfiles[member.friendId]
                            val phone = friendProfile?.phone ?: ""

                            BalanceCard(
                                name = member.name,
                                avatarColorIndex = friendProfile?.avatarColorIndex ?: 0,
                                balance = balance,
                                phone = phone,
                                isCurrentUser = member.isCurrentUser,
                                onWhatsAppClick = {
                                    val amountStr = MoneyFormatter.formatINR(abs(balance))
                                    val groupName = state.group?.name ?: ""
                                    // Custom message depending on whether A owes us (balance < 0) or we owe A (balance > 0)
                                    val msg = if (balance > 0) {
                                        "Hi ${member.name}, I need to give you $amountStr for our group \"$groupName\"."
                                    } else {
                                        "Hi ${member.name}, you need to give me $amountStr for our group \"$groupName\"."
                                    }
                                    sendWhatsApp(context, phone, msg)
                                }
                            )
                        }
                    }
                }

                // TAB 2: SETTLE UP
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Settle Up Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.simplifiedDebts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All settled up! No active debts.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.simplifiedDebts) { (fromId, toId, amountMinor) ->
                                    val fromMember = state.members.find { it.id == fromId }
                                    val toMember = state.members.find { it.id == toId }
                                    
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
                                                MemberAvatar(name = fromMember?.name ?: "", colorIndex = 0, size = 32.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(fromMember?.name ?: "", fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("➔", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                MemberAvatar(name = toMember?.name ?: "", colorIndex = 1, size = 32.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(toMember?.name ?: "", fontWeight = FontWeight.Medium)
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = MoneyFormatter.formatINR(amountMinor),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        initialFromMemberId = fromId
                                                        initialToMemberId = toId
                                                        initialAmount = amountMinor / 100.0
                                                        viewModel.toggleSettleUp(true)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Payment,
                                                        contentDescription = "Settle Payment",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                initialFromMemberId = ""
                                initialToMemberId = ""
                                initialAmount = 0.0
                                viewModel.toggleSettleUp(true)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Record Settlement")
                        }
                    }
                }
            }
        }
    }

    if (state.showAddExpense) {
        AddGroupExpenseSheet(
            members = state.members,
            onDismiss = { viewModel.toggleAddExpense(false) },
            onSave = { description, amount, paidByMemberId, category ->
                viewModel.addExpense(description, amount, paidByMemberId, category)
            }
        )
    }

    if (state.showSettleUp) {
        SettleUpSheet(
            members = state.members,
            onDismiss = { viewModel.toggleSettleUp(false) },
            onSave = { fromMemberId, toMemberId, amount ->
                viewModel.recordSettlement(fromMemberId, toMemberId, amount)
            },
            initialFromMemberId = initialFromMemberId,
            initialToMemberId = initialToMemberId,
            initialAmountRupees = initialAmount
        )
    }

    if (state.showAddMemberDialog) {
        val selectedFriendIds = remember { mutableStateListOf<String>() }
        val groupFriendIds = state.members.map { it.friendId }.toSet()
        val addableFriends = state.allFriends.filter { it.id !in groupFriendIds }

        AlertDialog(
            onDismissRequest = { viewModel.toggleAddMember(false) },
            title = { Text("Add Members") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select friends to add to this group:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (addableFriends.isEmpty()) {
                        Text(
                            text = "All friends are already members of this group.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(addableFriends) { friend ->
                                val isSelected = selectedFriendIds.contains(friend.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedFriendIds.remove(friend.id)
                                            else selectedFriendIds.add(friend.id)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked == true) selectedFriendIds.add(friend.id)
                                            else selectedFriendIds.remove(friend.id)
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedFriendIds.isNotEmpty()) {
                            viewModel.addMembersToGroup(selectedFriendIds.toList())
                        } else {
                            viewModel.toggleAddMember(false)
                        }
                    },
                    enabled = addableFriends.isNotEmpty() && selectedFriendIds.isNotEmpty()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddMember(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun sendWhatsApp(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        val url = "https://wa.me/$cleanPhone?text=${URLEncoder.encode(message, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback or alert if WhatsApp is not installed
        val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
        }
        context.startActivity(sendIntent)
    }
}
