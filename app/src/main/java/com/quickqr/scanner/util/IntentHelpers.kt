package com.quickqr.scanner.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.quickqr.scanner.R

/**
 * Open and optional passkey hand-off via [Intent.ACTION_VIEW].
 *
 * We do not implement proprietary crypto or a custom WebAuthn/caBLE stack.
 * For FIDO hybrid QR payloads we fire ACTION_VIEW on the raw URI so Play
 * Services / the system FIDO path can continue the flow when available.
 */
object IntentHelpers {

    fun openUri(context: Context, raw: String): Boolean {
        return launchView(context, raw, failureMessageRes = R.string.open_no_handler)
    }

    fun pairWithPasskey(context: Context, raw: String): Boolean {
        return launchView(context, raw, failureMessageRes = R.string.pair_no_handler)
    }

    private fun launchView(context: Context, raw: String, failureMessageRes: Int): Boolean {
        val uri = Uri.parse(raw.trim())
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            val resolve = intent.resolveActivity(context.packageManager)
            if (resolve == null) {
                Toast.makeText(context, failureMessageRes, Toast.LENGTH_LONG).show()
                false
            } else {
                context.startActivity(intent)
                true
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, failureMessageRes, Toast.LENGTH_LONG).show()
            false
        } catch (_: SecurityException) {
            Toast.makeText(context, failureMessageRes, Toast.LENGTH_LONG).show()
            false
        }
    }
}
