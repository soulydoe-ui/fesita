package com.fiestast.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.DeepBlack
import com.fiestast.launcher.ui.theme.Graphite
import com.fiestast.launcher.ui.theme.PureWhite

enum class AutomotiveButtonStyle {
    PRIMARY,
    SECONDARY,
    OUTLINE
}

@Composable
fun AutomotiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AutomotiveButtonStyle = AutomotiveButtonStyle.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "automotive_button"
) {
    val shape = RoundedCornerShape(8.dp)

    when (style) {
        AutomotiveButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightRed,
                    contentColor = PureWhite,
                    disabledContainerColor = Graphite,
                    disabledContentColor = Color.Gray
                ),
                modifier = modifier
                    .testTag(testTag)
                    .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = text.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        AutomotiveButtonStyle.SECONDARY -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Graphite,
                    contentColor = PureWhite
                ),
                modifier = modifier
                    .testTag(testTag)
                    .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = text.uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.75.sp
                )
            }
        }
        AutomotiveButtonStyle.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PureWhite
                ),
                modifier = modifier
                    .testTag(testTag)
                    .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = text.uppercase(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
