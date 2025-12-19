package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip


import androidx.compose.foundation.layout.Box

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.app.mobile.R
import com.app.mobile.ui.theme.Dimens

@Composable
fun LogoCircle() {
    Box(
        modifier = Modifier
            .size(Dimens.LogoCircleSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface) // Фон внутри круга
            .border(
                width = Dimens.BorderWidthThick,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
//        Icon(
//            imageVector = Icons.Default.Settings,
//            contentDescription = null,
//            modifier = Modifier.size(Dimens.LogoCircleLogoSize),
//            tint = Color.Black
//        )
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Логотип пчелы",
            modifier = Modifier.size(Dimens.LogoCircleLogoSize)
        )
    }
}