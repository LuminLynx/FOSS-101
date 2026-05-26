package com.perpenda.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Editorial = mostly square; 2–8dp only. Transcribed from the design system
// radii tokens (colors_and_type.css): r-1 2dp fields/chips, r-2 4dp buttons,
// r-3 8dp sheet/modal. The prior 8–28dp rounding is replaced per the brief.
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)
