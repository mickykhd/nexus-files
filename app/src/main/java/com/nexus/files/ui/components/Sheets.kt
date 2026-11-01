package com.nexus.files.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

/** The 32×4 rounded drag handle specified by the design system. */
@Composable
fun SheetDragHandle(modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "handle",
    )
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .width(32.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
        )
    }
}

/**
 * Standard bottom sheet shell: rounded-t-3xl surface, drag handle, dismissible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocalNexusExtra.current.surfaceMid,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { SheetDragHandle() },
        scrimColor = LocalNexusExtra.current.scrim,
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) { content() }
    }
}

/** Sheet title block: icon + title + subtitle + trailing close. */
@Composable
fun SheetHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    trailingTag: (@Composable () -> Unit)? = null,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                )
                if (trailingTag != null) {
                    Spacer(Modifier.width(8.dp))
                    trailingTag()
                }
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val extra = LocalNexusExtra.current
        androidx.compose.material3.TextButton(onClick = onClose) {
            Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
        }
    }
    Spacer(Modifier.height(8.dp))
}
