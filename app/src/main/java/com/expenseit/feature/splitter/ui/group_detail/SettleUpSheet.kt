package com.expenseit.feature.splitter.ui.group_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenseit.core.model.GroupMemberEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpSheet(
    members: List<GroupMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (fromMemberId: String, toMemberId: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier,
    initialFromMemberId: String = "",
    initialToMemberId: String = "",
    initialAmountRupees: Double = 0.0
) {
    val sheetState = rememberModalBottomSheetState()
    
    var selectedFrom by remember {
        mutableStateOf(members.find { it.id == initialFromMemberId } ?: members.firstOrNull())
    }
    
    var selectedTo by remember {
        mutableStateOf(members.find { it.id == initialToMemberId } ?: members.getOrNull(1) ?: members.firstOrNull())
    }
    
    var amount by remember {
        mutableStateOf(if (initialAmountRupees > 0) String.format("%.2f", initialAmountRupees) else "")
    }

    var isFromDropdownExpanded by remember { mutableStateOf(false) }
    var isToDropdownExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Record payment",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // From Selection Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedFrom?.name ?: "",
                        onValueChange = {},
                        label = { Text("From") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isFromDropdownExpanded = true }) {
                                Icon(
                                    imageVector = if (isFromDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { isFromDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isFromDropdownExpanded,
                        onDismissRequest = { isFromDropdownExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedFrom = member
                                    isFromDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // To Selection Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedTo?.name ?: "",
                        onValueChange = {},
                        label = { Text("To") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isToDropdownExpanded = true }) {
                                Icon(
                                    imageVector = if (isToDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { isToDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isToDropdownExpanded,
                        onDismissRequest = { isToDropdownExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedTo = member
                                    isToDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
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
                        val amountVal = amount.toDoubleOrNull()
                        if (amountVal == null || amountVal <= 0.0) {
                            errorMsg = "Enter an amount greater than 0."
                        } else if (selectedFrom == null || selectedTo == null) {
                            errorMsg = "Select both sender and receiver."
                        } else if (selectedFrom!!.id == selectedTo!!.id) {
                            errorMsg = "Cannot pay yourself."
                        } else {
                            errorMsg = ""
                            onSave(selectedFrom!!.id, selectedTo!!.id, amountVal)
                        }
                    }
                ) {
                    Text("Record payment")
                }
            }
        }
    }
}
