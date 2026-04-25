package com.codingindia.messanger.features.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateHeader(timestamp: Long) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = formatDate(timestamp),
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()

    // Check if the message was sent today
    if (now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == messageDate.get(
            Calendar.DAY_OF_YEAR
        )
    ) {
        return "Today"
    }

    // Check if the message was sent yesterday
    now.add(Calendar.DAY_OF_YEAR, -1) // Decrement 'now' by one day
    if (now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == messageDate.get(
            Calendar.DAY_OF_YEAR
        )
    ) {
        return "Yesterday"
    }

    // Otherwise, return the full date
    return SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
}
