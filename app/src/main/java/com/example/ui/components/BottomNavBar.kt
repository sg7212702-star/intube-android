package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InTubeTab
import com.example.ui.theme.InTubeBottomBarBg
import com.example.ui.theme.InTubeCardBorder
import com.example.ui.theme.InTubeGold
import com.example.ui.theme.InTubePink
import com.example.ui.theme.InTubePinkGlow
import com.example.ui.theme.InTubeTextMuted

@Composable
fun BottomNavBar(
    currentTab: InTubeTab,
    unreadNotifications: Int,
    onTabSelected: (InTubeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(InTubeBottomBarBg)
                .border(1.dp, InTubeCardBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            BottomNavItem(
                icon = if (currentTab == InTubeTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentTab == InTubeTab.HOME,
                testTag = "tab_home",
                onClick = { onTabSelected(InTubeTab.HOME) }
            )

            // Explore Tab
            BottomNavItem(
                icon = if (currentTab == InTubeTab.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = "Explore",
                isSelected = currentTab == InTubeTab.EXPLORE,
                testTag = "tab_explore",
                onClick = { onTabSelected(InTubeTab.EXPLORE) }
            )

            // Center Create Button (Glowing Neon Pink Gradient)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(InTubePink, Color(0xFFFF2A8D))
                        )
                    )
                    .border(2.dp, InTubePinkGlow, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onTabSelected(InTubeTab.CREATE)
                    }
                    .testTag("tab_create"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reel",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Activity / Notifications Tab with Badge
            Box(contentAlignment = Alignment.Center) {
                BottomNavItem(
                    icon = if (currentTab == InTubeTab.NOTIFICATIONS) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = "Activity",
                    isSelected = currentTab == InTubeTab.NOTIFICATIONS,
                    testTag = "tab_activity",
                    onClick = { onTabSelected(InTubeTab.NOTIFICATIONS) }
                )

                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(16.dp)
                            .background(Color(0xFFFF0055), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotifications > 9) "9+" else "$unreadNotifications",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Profile Tab
            BottomNavItem(
                icon = if (currentTab == InTubeTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                label = "Profile",
                isSelected = currentTab == InTubeTab.PROFILE,
                testTag = "tab_profile",
                onClick = { onTabSelected(InTubeTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) InTubePink else InTubeTextMuted,
        label = "nav_icon_tint"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag(testTag)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
