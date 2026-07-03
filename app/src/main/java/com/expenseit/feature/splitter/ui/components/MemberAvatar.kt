package com.expenseit.feature.splitter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenseit.ui.theme.AvatarColors

@Composable
fun MemberAvatar(
    name: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val displayColor = AvatarColors.getOrNull(colorIndex % AvatarColors.size) ?: AvatarColors[0]
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(displayColor)
    ) {
        Text(
            text = initial.toString(),
            color = Color.White,
            fontSize = (size.value * 0.45).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
