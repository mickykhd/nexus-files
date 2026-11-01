package com.nexus.files.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.ui.components.TickGlyph
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion
import kotlinx.coroutines.launch

/** Rename / create-folder prompt with a springy entrance. */
@Composable
fun NameDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    var name by remember { mutableStateOf(initial) }
    val pop by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dialogPop",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = extra.surfaceMid,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop },
        title = {
            Text(
                title,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = MonoFontFamily),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = extra.docs,
                    unfocusedBorderColor = extra.outlineSoft,
                    focusedContainerColor = extra.surfaceHigh,
                    unfocusedContainerColor = extra.surfaceHigh,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name", fontFamily = MonoFontFamily) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text(confirmLabel, fontFamily = MonoFontFamily, fontWeight = FontWeight.W700, color = extra.docs)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

enum class PinFlow { SETUP, ENTER }

/** Vault PIN setup / verification with a playful shake on failure. */
@Composable
fun PinDialog(
    setup: Boolean,
    onVerify: suspend (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var shake by remember { mutableStateOf(0f) }
    val shakeOffset by animateFloatAsState(
        targetValue = shake,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "shake",
    )
    val pop by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pinPop",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = extra.surfaceMid,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.graphicsLayer {
            scaleX = pop; scaleY = pop
            translationX = shakeOffset * 24f
        },
        title = {
            Text(
                if (setup) "Set Vault PIN" else "Unlock Vault",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column {
                Text(
                    if (setup) "Choose a 4-digit PIN for the zero-knowledge enclave."
                    else "Enter your vault PIN to decrypt the enclave index.",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4) {
                            pin = it.filter { c -> c.isDigit() }
                            error = false
                        }
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = MonoFontFamily,
                        textAlign = TextAlign.Center,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (error) extra.danger else extra.docs,
                        unfocusedBorderColor = if (error) extra.danger else extra.outlineSoft,
                        focusedContainerColor = extra.surfaceHigh,
                        unfocusedContainerColor = extra.surfaceHigh,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PIN", fontFamily = MonoFontFamily) },
                )
                if (error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Incorrect PIN — try again",
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = extra.danger,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = pin.length == 4,
                onClick = {
                    scope.launch {
                        val ok = onVerify(pin)
                        if (!ok) {
                            error = true
                            shake = 1f
                            kotlinx.coroutines.delay(90)
                            shake = -1f
                            kotlinx.coroutines.delay(90)
                            shake = 0f
                        }
                    }
                },
            ) {
                TickGlyph(progress = 1f, color = extra.docs, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(6.dp))
                Text(
                    if (setup) "Protect Vault" else "Unlock",
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    color = extra.docs,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
