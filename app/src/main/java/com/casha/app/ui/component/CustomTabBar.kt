package com.casha.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
// Use placeholder icons if precise equivalents aren't available in default Compose icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val tag: Int,
    val isCenterButton: Boolean = false,
    val action: (() -> Unit)? = null
)

@Composable
fun CustomTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background with frosted glass effect
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
            shadowElevation = 16.dp
        ) {
            // Optional blur effect for frosted glass, Note: Modifier.blur only works on Android 12+
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(radius = 16.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                if (tab.isCenterButton) {
                    CenterTabButton(tab = tab)
                } else {
                    RegularTabButton(
                        tab = tab,
                        isSelected = selectedTab == tab.tag,
                        onClick = { onTabSelected(tab.tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterTabButton(tab: TabItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale_anim"
    )

    Box(
        modifier = Modifier
            .offset(y = (-20).dp)
            .size(64.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { tab.action?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background shadow & circle
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.RegularTabButton(
    tab: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else (if (isSelected) 1.05f else 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tab_scale_anim"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
        label = "tab_color_anim"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                )
            }
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = tab.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor,
            modifier = Modifier.scale(scale)
        )
    }
}
