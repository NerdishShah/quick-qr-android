package com.quickqr.scanner.ui.result

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.quickqr.scanner.R
import com.quickqr.scanner.model.QrContentType
import com.quickqr.scanner.util.IntentHelpers
import com.quickqr.scanner.util.QrClassifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    rawValue: String,
    onScanAgain: () -> Unit
) {
    val context = LocalContext.current
    val payload = remember(rawValue) { QrClassifier.classify(rawValue) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (payload.contentType) {
                                QrContentType.URL -> stringResource(R.string.chip_url)
                                QrContentType.FIDO_PASSKEY -> stringResource(R.string.chip_fido)
                                QrContentType.TEXT -> stringResource(R.string.chip_text)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            when (payload.contentType) {
                                QrContentType.URL -> Icons.Outlined.Link
                                QrContentType.FIDO_PASSKEY -> Icons.Outlined.Key
                                QrContentType.TEXT -> Icons.Outlined.QrCode2
                            },
                            contentDescription = null
                        )
                    }
                )
            }

            Text(
                text = payload.rawValue,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { IntentHelpers.openUri(context, payload.rawValue) },
                enabled = payload.canOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Link, contentDescription = null)
                Text(
                    stringResource(R.string.action_open),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (payload.canPair) {
                FilledTonalButton(
                    onClick = { IntentHelpers.pairWithPasskey(context, payload.rawValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Key, contentDescription = null)
                    Text(
                        stringResource(R.string.action_pair),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    val cm = context.getSystemService<ClipboardManager>()
                    cm?.setPrimaryClip(ClipData.newPlainText("qr", payload.rawValue))
                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                Text(
                    stringResource(R.string.action_copy),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedButton(
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.QrCode2, contentDescription = null)
                Text(
                    stringResource(R.string.action_scan_again),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
