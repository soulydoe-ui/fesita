package com.fiestast.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.theme.Graphite
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite
import com.fiestast.launcher.ui.theme.StatusAmber
import com.fiestast.launcher.ui.theme.StatusDisabled
import com.fiestast.launcher.ui.theme.StatusGreen
import com.fiestast.launcher.ui.theme.StatusRed

@Composable
fun StatusIndicator(
    status: ServiceStatus,
    label: String,
    modifier: Modifier = Modifier
) {
    val (dotColor, statusText) = when (status) {
        is ServiceStatus.Connected -> StatusGreen to "Connected"
        is ServiceStatus.Available -> StatusGreen to status.message
        is ServiceStatus.Unavailable -> StatusDisabled to status.reason
        is ServiceStatus.Demo -> StatusAmber to "[DEMO] ${status.message}"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Graphite.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            color = LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = statusText,
            color = PureWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
