package com.nexus.files.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.ui.components.TickGlyph
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

/**
 * Real storage-access gate. The app cannot show files until the user grants
 * "All files access" (Android 11+) or the classic storage permissions
 * (Android 10) — this screen drives that real system flow.
 */
@Composable
fun PermissionScreen(
    needsLegacy: Boolean,
    onOpenSettings: () -> Unit,
    onRecheck: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    val context = LocalContext.current

    val legacyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { onRecheck() }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { onRecheck() }

    val pulse by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "shieldPulse",
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        // Big animated shield
        androidx.compose.foundation.layout.Box(
            Modifier
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .size(110.dp)
                .background(extra.docs.copy(alpha = 0.14f), CircleShape)
                .border(2.dp, extra.docs.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                tint = extra.docs,
                modifier = Modifier.size(52.dp),
            )
        }

        Spacer(Modifier.height(26.dp))
        Text(
            "Storage access required",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Nexus Files needs real access to your device storage to browse, clean and vault your files. Nothing is read until you grant it.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = MonoFontFamily,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(26.dp))

        val bullets = listOf(
            Triple(Icons.Rounded.Folder, "Browse & manage files", "Full file operations on your storage"),
            Triple(Icons.Rounded.Security, "Analyze & clean", "Find junk, duplicates and large files"),
            Triple(Icons.Rounded.Lock, "Encrypt into the vault", "AES-256-GCM, hardware-backed key"),
        )
        bullets.forEachIndexed { index, (icon, title, sub) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .enterStagger(index, 60)
                    .background(extra.surfaceLow, RoundedCornerShape(18.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = extra.docs, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        sub,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                if (needsLegacy) {
                    legacyLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ),
                    )
                } else {
                    settingsLauncher.launch(com.nexus.files.data.FileRepository(context).storageAccessIntent())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = extra.docs,
                contentColor = Color(0xFF00363D),
            ),
        ) {
            Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                if (needsLegacy) "Grant storage permission" else "Grant all files access",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                "Opens Android's \"All files access\" page for Nexus Files."
            } else {
                "Opens the Android storage permission prompt."
            },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
