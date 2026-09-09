package com.fiestast.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite

data class DockItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

val DockItems = listOf(
    DockItem(NavRoutes.HOME, "Home", Icons.Default.Home, "dock_home"),
    DockItem(NavRoutes.NAVIGATION, "Navigation", Icons.Default.NearMe, "dock_nav"),
    DockItem(NavRoutes.MUSIC, "Music", Icons.Default.MusicNote, "dock_music"),
    DockItem(NavRoutes.PHONE, "Phone", Icons.Default.Phone, "dock_phone"),
    DockItem(NavRoutes.RADIO, "Radio", Icons.Default.Radio, "dock_radio"),
    DockItem(NavRoutes.APPS, "Apps", Icons.Default.Apps, "dock_apps"),
    DockItem(NavRoutes.SETTINGS, "Settings", Icons.Default.Settings, "dock_settings"),
    DockItem(NavRoutes.ZLINK, "ZLink", Icons.Default.Smartphone, "dock_zlink")
)

/**
 * Bottom Dock matching reference mockup:
 * - Edge-to-edge dark translucent bar
 * - 8 items: Home, Navigation, Music, Phone, Radio, Apps, Settings, ZLink
 * - Active state: red glowing pill, red icon, bold white text, bottom glowing red accent line
 */
@Composable
fun BottomDock(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF07080B))
            .border(width = 1.dp, color = CardBorder.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockItems.forEach { item ->
            val isSelected = currentRoute == item.route
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) BrightRed else LightGray.copy(alpha = 0.85f),
                label = "dock_icon_color"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PureWhite else LightGray.copy(alpha = 0.75f),
                label = "dock_text_color"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isSelected) {
                                listOf(
                                    Color(0xFFB91C1C).copy(alpha = 0.40f),
                                    Color(0xFF7F1D1D).copy(alpha = 0.15f)
                                )
                            } else {
                                listOf(Color.Transparent, Color.Transparent)
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) BrightRed.copy(alpha = 0.8f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onNavigate(item.route) }
                    .testTag(item.testTag),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = item.title,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(width = 24.dp, height = 2.5.dp)
                                .background(BrightRed, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}
