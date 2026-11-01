package com.nexus.files.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

private fun findActivity(context: Context): FragmentActivity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Real status of device biometrics / credentials. */
fun biometricStatus(context: Context): Int =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS)

/**
 * Shows the real system biometric / device-credential prompt.
 * [onPinFallback] runs when the device has no biometrics or credential
 * enrolled, so the app can fall back to its vault PIN.
 */
fun authenticate(
    context: Context,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    onPinFallback: () -> Unit,
) {
    val activity = findActivity(context) ?: run { onPinFallback(); return }
    if (biometricStatus(context) != BiometricManager.BIOMETRIC_SUCCESS) {
        onPinFallback()
        return
    }
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User cancelled or locked out — fall back to PIN only when the
                // hardware is missing; otherwise report the failure honestly.
                if (errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ||
                    errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                    errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE
                ) onPinFallback() else onFailure()
            }
            override fun onAuthenticationFailed() = onFailure()
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()
    prompt.authenticate(info)
}
