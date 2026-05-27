package com.perpenda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perpenda.ui.theme.PerpendaTheme

enum class CalibrationTier { Settled, Contested, Unsettled }

// Calibration chip — flat tint, 2dp corner, uppercase UI micro-label.
// Tiers map to the marginal-ink accents (sage / ochre / mineral).
@Composable
fun TagChip(
    label: String,
    modifier: Modifier = Modifier,
    tier: CalibrationTier = CalibrationTier.Unsettled
) {
    val colors = PerpendaTheme.colors
    val (container, content) = when (tier) {
        CalibrationTier.Settled -> colors.settledTint to colors.onSettledTint
        CalibrationTier.Contested -> colors.contestedTint to colors.onContestedTint
        CalibrationTier.Unsettled -> colors.unsettledTint to colors.onUnsettledTint
    }
    Text(
        text = label.uppercase(),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        color = content,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.0.sp
        ),
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
