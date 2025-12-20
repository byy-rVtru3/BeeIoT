package com.app.mobile.presentation.ui.components


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.app.mobile.R
import com.app.mobile.presentation.ui.modifiers.styleShadow
import com.app.mobile.presentation.ui.screens.aboutapp.AboutAppRoute

import com.app.mobile.presentation.ui.screens.hive.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.queen.list.QueenListRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute
import com.app.mobile.ui.theme.Dimens


data class BottomTabItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

@Composable
fun AppBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit
) {
    val tabs = listOf(
        BottomTabItem(
            stringResource(R.string.home),
            ImageVector.vectorResource(R.drawable.ic_home),
            AboutAppRoute
        ),

        BottomTabItem(
            stringResource(R.string.sensors),
            ImageVector.vectorResource(R.drawable.ic_sensors),
            AboutAppRoute
        ),

        BottomTabItem(
            stringResource(R.string.queens), ImageVector.vectorResource(R.drawable.ic_queens),
            QueenListRoute
        ),
        BottomTabItem(
            stringResource(R.string.hives),
            ImageVector.vectorResource(R.drawable.ic_hives),
            HivesListRoute
        ),
        BottomTabItem(
            stringResource(R.string.settings),
            ImageVector.vectorResource(R.drawable.ic_settings),
            SettingsRoute
        )
    )

    val activeBorderColor = MaterialTheme.colorScheme.primary
    val indicatorShape = RoundedCornerShape(Dimens.BorderRadiusMedium)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.Null,
        modifier = Modifier
            .styleShadow()
            .clip(RoundedCornerShape(Dimens.BorderRadiusMedium, Dimens.BorderRadiusMedium))
            .height(Dimens.BottomAppBarHeight)

    ) {
        Spacer(modifier = Modifier.width(Dimens.BottomAppBarHorizontalPadding))

        tabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any {
                it.hasRoute(tab.route::class)
            } == true


            CustomBottomNavigationItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = tab.icon,
                label = tab.label,
                activeBorderColor = activeBorderColor,
                shape = indicatorShape,

                )
        }

        Spacer(modifier = Modifier.width(Dimens.BottomAppBarHorizontalPadding))

    }
}

@Composable
fun RowScope.CustomBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    activeBorderColor: Color,
    shape: RoundedCornerShape
) {

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier

                    .clip(shape)
                    .border(
                        width = if (selected) Dimens.BorderWidthNormal else Dimens.Null,
                        color = if (selected) activeBorderColor else Color.Transparent,
                        shape = shape
                    )
                    .clickable(onClick = onClick)
                    .padding(
                        horizontal = Dimens.BottomAppBarHorizontalButtonPadding,
                        vertical = Dimens.BottomAppBarVerticalButtonPadding
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(Dimens.BottomAppBarIconSize),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimens.BottomAppBarTextPadding)
            )
        }
    }
}


@Preview
@Composable
fun AppBottomBarPreview() {
    AppBottomBar(
        currentDestination = null,
        onNavigate = {}
    )
}