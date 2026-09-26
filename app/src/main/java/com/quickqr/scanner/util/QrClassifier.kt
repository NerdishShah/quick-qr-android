package com.quickqr.scanner.util

import android.net.Uri
import android.util.Patterns
import com.quickqr.scanner.model.QrContentType
import com.quickqr.scanner.model.ScanPayload
import java.util.Locale

/**
 * Classifies scanned QR text for Open / optional Pair affordances.
 *
 * Most codes are plain text or http(s) URLs. A minority are FIDO hybrid /
 * caBLE-style device-pairing URIs (typically `FIDO:/...`); those unlock the
 * secondary "Pair with passkey" action.
 */
object QrClassifier {

    private val fidoPrefixes = listOf(
        "FIDO:/",
        "FIDO:",
        "FIDO2:/",
        "CABLE:",
        "HYBRID:"
    )

    fun classify(raw: String): ScanPayload {
        val trimmed = raw.trim()
        val upper = trimmed.uppercase(Locale.US)

        val isFido = fidoPrefixes.any { upper.startsWith(it) }
        val isHttpUrl = isHttpOrHttps(trimmed)
        val isActionableUri = canTreatAsViewIntent(trimmed)

        val type = when {
            isFido -> QrContentType.FIDO_PASSKEY
            isHttpUrl || isActionableUri -> QrContentType.URL
            else -> QrContentType.TEXT
        }

        return ScanPayload(
            rawValue = trimmed,
            contentType = type,
            // Open for http(s) and other actionable deep links (not FIDO — Pair handles those)
            canOpen = isHttpUrl || (isActionableUri && !isFido),
            canPair = isFido
        )
    }

    fun isHttpOrHttps(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase(Locale.US) ?: return false
        if (scheme != "http" && scheme != "https") return false
        return Patterns.WEB_URL.matcher(value).matches() || !uri.host.isNullOrBlank()
    }

    fun canTreatAsViewIntent(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        val scheme = uri.scheme ?: return false
        if (scheme.isBlank()) return false
        return value.contains(":") && !scheme.equals("javascript", ignoreCase = true)
    }
}
