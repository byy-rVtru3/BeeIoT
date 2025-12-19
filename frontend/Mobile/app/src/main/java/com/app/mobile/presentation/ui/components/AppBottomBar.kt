package com.app.mobile.presentation.ui.components


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

import com.app.mobile.presentation.ui.screens.hive.list.HivesListRoute
import com.app.mobile.presentation.ui.screens.settings.SettingsRoute


data class BottomTabItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

//@Composable
//fun AppBottomBar(
//    currentDestination: NavDestination?,
//    onNavigate: (Any) -> Unit
//) {
//    val tabs = listOf(
////        BottomTabItem("Главная", Icons.Outlined.Home, HivesListRoute),
////        BottomTabItem("Датчики", Icons.Outlined.Sensors, SettingsRoute),
////        BottomTabItem("Матки", Icons.Outlined.Settings, SettingsRoute),
//        BottomTabItem("Ульи", Icons.Outlined.Inbox, HivesListRoute),
//        BottomTabItem("Настройки", Icons.Outlined.Settings, SettingsRoute)
//    )
//
//
//    NavigationBar(
//        containerColor = MaterialTheme.colorScheme.surface,
//        tonalElevation = 0.dp,
//        modifier = Modifier
//            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = Dimens.BorderRadiusMedium, topEnd = Dimens.BorderRadiusMedium))
//            .clip(RoundedCornerShape(topStart = Dimens.BorderRadiusMedium, topEnd = Dimens.BorderRadiusMedium))
//            .height(80.dp)
//    ) {
//        tabs.forEach { tab ->
//            val selected = currentDestination?.hierarchy?.any {
//                it.hasRoute(tab.route::class)
//            } == true
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = { onNavigate(tab.route) },
//                icon = {
//                    Icon(
//                        imageVector = tab.icon,
//                        contentDescription = tab.label,
//                        modifier = Modifier.size(32.dp)
//                    )
//                },
//                label = {
//                    Text(
//                        text = tab.label,
//                        style = MaterialTheme.typography.labelSmall.copy(
//                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
//                        )
//                    )
//                },
//                alwaysShowLabel = true,
//
//                colors = NavigationBarItemDefaults.colors(
//                    indicatorColor = MaterialTheme.colorScheme.primary,
//
//                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
//
//                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        }
//    }
//}


@Composable
fun AppBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit
) {
    val tabs = listOf(
        BottomTabItem("Ульи", Icons.Outlined.Inbox, HivesListRoute),
        BottomTabItem("Настройки", Icons.Outlined.Settings, SettingsRoute)
    )

    val activeBorderColor = Color(0xFFFFC107)
    val indicatorShape = RoundedCornerShape(16.dp)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .height(80.dp)
    ) {
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
                shape = indicatorShape
            )
        }
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
                        width = if (selected) 2.dp else 0.dp,
                        color = if (selected) activeBorderColor else Color.Transparent,
                        shape = shape
                    )
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
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