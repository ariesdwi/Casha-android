package com.casha.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Tab Item Model ──

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val tag: Int,
    val isCenterButton: Boolean = false,
    val onAction: (() -> Unit)? = null
)

// ── Custom Tab Bar ──

@Composable
fun CustomTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    // Outer Box has layout bounds expanded to fit the FAB
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 36.dp)
    ) {
        // Inner background with rounded shape and shadow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                if (tab.isCenterButton) {
                    // Placeholder box to maintain spacing in the Row
                    Box(modifier = Modifier.weight(1f))
                } else {
                    RegularTabButton(
                        tab = tab,
                        isSelected = selectedTab == tab.tag,
                        primaryColor = primaryColor,
                        onTabSelected = onTabSelected,
                        haptic = haptic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Overlay the center button on top of everything
        tabs.firstOrNull { it.isCenterButton }?.let { centerTab ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                CenterAddButton(
                    tab = centerTab,
                    primaryColor = primaryColor,
                    haptic = haptic
                )
            }
        }
    }
}

// ── Center "Add" Button ──

@Composable
private fun CenterAddButton(
    tab: TabItem,
    primaryColor: Color,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "centerScale"
    )

    // Darker green for the FAB to match the reference
    val fabColor = primaryColor // Uses theme primary (CashaPrimary)

    Box(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                tab.onAction?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        // Shadow + White Ring + Green Circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .background(Color.White, CircleShape) // The white ring effect
                .padding(4.dp) // Ring thickness
                .background(fabColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Plus icon
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ── Regular Tab Button ──

@Composable
private fun RegularTabButton(
    tab: TabItem,
    isSelected: Boolean,
    primaryColor: Color,
    onTabSelected: (Int) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isSelected -> 1.1f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else Color.Gray,
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else Color.Gray,
        label = "textColor"
    )

    val highlightSize by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "highlightSize"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "pressAlpha"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTabSelected(tab.tag)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with highlight background
        Box(
            modifier = Modifier
                .size(48.dp) // Slightly smaller to give labels more room
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            // Animated highlight circle - matches reference house background
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f))
                )
            }

            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Label
        Text(
            text = tab.title,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.graphicsLayer {
                scaleX = if (isSelected) 1.05f else 1f
                scaleY = if (isSelected) 1.05f else 1f
            }
        )
    }
}
