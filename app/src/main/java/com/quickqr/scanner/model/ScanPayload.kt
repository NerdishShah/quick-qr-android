package com.quickqr.scanner.model

data class ScanPayload(
    val rawValue: String,
    val contentType: QrContentType,
    val canOpen: Boolean,
    val canPair: Boolean
)
