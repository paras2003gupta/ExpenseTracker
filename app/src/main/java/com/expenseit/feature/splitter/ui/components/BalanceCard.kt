package com.expenseit.feature.splitter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenseit.core.util.MoneyFormatter
import kotlin.math.abs

@Composable
fun BalanceCard(
    name: String,
    avatarColorIndex: Int,
    balance: Long, // in paise. Positive = they are owed (gets back), Negative = they owe
    phone: String,
    isCurrentUser: Boolean,
    onWhatsAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (balance > 0) {
        Color(0xFFECFDF5) // Light emerald green
    } else if (balance < 0) {
        Color(0xFFFEF2F2) // Light red
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textCol = if (balance > 0) {
        Color(0xFF047857) // Emerald green
    } else if (balance < 0) {
        Color(0xFFB91C1C) // Red
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(name = name, colorIndex = avatarColorIndex)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isCurrentUser) "$name (You)" else name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            balance > 0 -> "gets back ${MoneyFormatter.formatINR(balance)}"
                            balance < 0 -> "owes ${MoneyFormatter.formatINR(abs(balance))}"
                            else -> "settled up"
                        },
                        fontSize = 13.sp,
                        color = textCol,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Show WhatsApp share button for non-current-users who have a phone and have a non-zero balance
            if (!isCurrentUser && phone.isNotEmpty() && balance != 0L) {
                IconButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .background(Color(0xFF25D366).copy(alpha = 0.15f), RoundedCornerShape(50))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send WhatsApp Reminder",
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
