package com.tinhtx.localplayerapplication.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Custom Shapes for Music App
object MusicShapes {
    val albumCover = RoundedCornerShape(12.dp)
    val playerCard = RoundedCornerShape(24.dp)
    val miniPlayer = RoundedCornerShape(16.dp)
    val bottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val searchBar = RoundedCornerShape(28.dp)
    val button = RoundedCornerShape(20.dp)
    val chip = RoundedCornerShape(16.dp)
    val dialog = RoundedCornerShape(20.dp)
}
