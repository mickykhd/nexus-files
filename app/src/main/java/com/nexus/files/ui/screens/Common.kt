package com.nexus.files.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.NexusTab
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.TagChip
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

/** Shared app top bar: menu · logo · title · context chip · gear · palette. */
@Composable
fun NexusTopBar(
    contextChip: String?,
    onMenu: () -> Unit,
    onSettings: () -> Unit,
    onTheme: () -> Unit,
    showAvatar: Boolean = false,
) {
    val extra = LocalNexusExtra.current
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarIcon(Icons.Rounded.Menu, onClick = onMenu, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(10.dp))
        IconSquircle(
            icon = Icons.Rounded.Shield,
            tint = extra.docs,
            background = extra.docs,
            size = 34.dp,
            corner = 11.dp,
            iconSize = 18.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Nexus Files",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (contextChip != null) {
            Spacer(Modifier.width(8.dp))
            TagChip(contextChip, color = extra.success, filled = true)
        }
        Spacer(Modifier.weight(1f))
        BarIcon(Icons.Rounded.Settings, onClick = onSettings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        BarIcon(Icons.Rounded.AutoAwesome, onClick = onTheme, tint = extra.docs)
        if (showAvatar) {
            Spacer(Modifier.width(6.dp))
            val avatarInteraction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(34.dp)
                    .background(extra.surfaceHighest, CircleShape)
                    .border(1.dp, extra.hairline, CircleShape)
                    .pressScale(avatarInteraction)
                    .tappable(avatarInteraction, onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun BarIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(36.dp)
            .pressScale(interaction, 0.88f)
            .tappable(interaction, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

data class NavItem(val tab: NexusTab, val label: String, val icon: ImageVector)

val NexusNavItems = listOf(
    NavItem(NexusTab.OVERVIEW, "Overview", Icons.Rounded.GridView),
    NavItem(NexusTab.FILES, "Files", Icons.Rounded.Folder),
    NavItem(NexusTab.CLEAN, "Clean", Icons.Rounded.CleaningServices),
    NavItem(NexusTab.VAULT, "Vault", Icons.Rounded.Lock),
)

/** Bottom navigation with a springy bubble indicator behind the active icon. */
@Composable
fun NexusBottomNav(
    selected: NexusTab,
    onSelect: (NexusTab) -> Unit,
) {
    val extra = LocalNexusExtra.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(extra.surfaceLowest)
            .border(1.dp, extra.hairline, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(top = 10.dp, bottom = 8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NexusNavItems.forEach { item ->
                val active = item.tab == selected
                val bubbleScale by animateFloatAsState(
                    targetValue = if (active) 1f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "navBubble",
                )
                val bubbleColor by animateColorAsState(
                    if (active) extra.docs else Color.Transparent,
                    tween(220), label = "navBubbleColor",
                )
                val iconTint by animateColorAsState(
                    if (active) Color(0xFF00363D) else MaterialTheme.colorScheme.onSurfaceVariant,
                    tween(220), label = "navIcon",
                )
                val iconScale by animateFloatAsState(
                    if (active) 1.08f else 1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "navIconScale",
                )
                val interaction = remember { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .pressScale(interaction, 0.9f)
                        .tappable(interaction) { onSelect(item.tab) }
                        .padding(horizontal = 10.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .graphicsLayer { scaleX = bubbleScale; scaleY = bubbleScale }
                                .background(bubbleColor, CircleShape),
                        )
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier
                                .size(23.dp)
                                .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.label,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.W700 else FontWeight.Medium,
                        color = if (active) extra.docs else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Animated toast that floats above the bottom navigation. */
@Composable
fun NexusToast(message: String?, onDone: () -> Unit) {
    val extra = LocalNexusExtra.current
    androidx.compose.animation.AnimatedVisibility(
        visible = message != null,
        enter = androidx.compose.animation.fadeIn(tween(240)) +
            androidx.compose.animation.slideInVertically(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                initialOffsetY = { it / 2 },
            ),
        exit = androidx.compose.animation.fadeOut(tween(180)) +
            androidx.compose.animation.slideOutVertically(tween(180), targetOffsetY = { it / 2 }),
        modifier = Modifier.navigationBarsPadding(),
    ) {
        androidx.compose.runtime.LaunchedEffect(message) {
            if (message != null) {
                kotlinx.coroutines.delay(2400)
                onDone()
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceHighest, RoundedCornerShape(18.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = extra.docs,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
