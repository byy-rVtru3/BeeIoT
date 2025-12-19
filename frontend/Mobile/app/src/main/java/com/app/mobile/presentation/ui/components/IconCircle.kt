package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.clip


import androidx.compose.foundation.layout.Box

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.app.mobile.ui.theme.Dimens

@Composable
fun IconCircle() {
    Box(
        modifier = Modifier
            .size(Dimens.IconCircleSize) // Размер круга
            .clip(CircleShape) // Обрезаем контент по кругу
            .background(MaterialTheme.colorScheme.surface) // Фон внутри круга
            .border(
                width = Dimens.BorderWidthThick,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Заменить иконку на нужную
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconCircleIconSize),
            tint = Color.Black
        )
    }
}