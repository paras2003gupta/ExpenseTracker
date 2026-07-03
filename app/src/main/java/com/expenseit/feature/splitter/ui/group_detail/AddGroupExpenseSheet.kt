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
import com.expenseit.core.model.Category
import com.expenseit.core.model.GroupMemberEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupExpenseSheet(
    members: List<GroupMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (description: String, amount: Double, paidByMemberId: String, category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    // Default payer to first member, or "You" if found
    var selectedPayer by remember {
        val you = members.find { it.isCurrentUser }
        mutableStateOf(you ?: members.firstOrNull())
    }
    
    var selectedCategory by remember { mutableStateOf(Category.OTHER.displayName) }
    
    var isPayerDropdownExpanded by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
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
                text = "Add group expense",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("e.g. Pizza night") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Paid By Selection Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedPayer?.name ?: "",
                        onValueChange = {},
                        label = { Text("Paid By") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isPayerDropdownExpanded = true }) {
                                Icon(
                                    imageVector = if (isPayerDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { isPayerDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isPayerDropdownExpanded,
                        onDismissRequest = { isPayerDropdownExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedPayer = member
                                    isPayerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Category Selection Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                            Icon(
                                imageVector = if (isCategoryDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { isCategoryDropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.emoji} ${category.displayName}") },
                            onClick = {
                                selectedCategory = category.displayName
                                isCategoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Split equally among all ${members.size} members",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
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
                        if (description.trim().isEmpty()) {
                            errorMsg = "Description is required."
                        } else if (amountVal == null || amountVal <= 0.0) {
                            errorMsg = "Enter an amount greater than 0."
                        } else if (selectedPayer == null) {
                            errorMsg = "Select who paid."
                        } else {
                            errorMsg = ""
                            onSave(description, amountVal, selectedPayer!!.id, selectedCategory)
                        }
                    }
                ) {
                    Text("Save expense")
                }
            }
        }
    }
}
