package com.akacaraka.herbal.ui

internal fun userFacingNotice(message: String, tone: StatusTone): String {
    val cleanMessage = message.trim()
    val lower = cleanMessage.lowercase()
    return when {
        cleanMessage.isBlank() -> "Belum ada pembaruan."
        lower.hasAny("google-services", "api key", "firebaseapp", "firebase belum", "configuration", "configured") -> "Firebase belum dikonfigurasi di perangkat ini. Mode demo lokal tetap bisa digunakan."
        lower.hasAny("network", "unable to resolve host", "timeout", "failed to connect", "unavailable") -> "Koneksi sedang bermasalah. Periksa internet lalu coba lagi."
        lower.hasAny("password is invalid", "invalid credential", "no user record", "user-not-found", "wrong-password", "invalid-email") -> "Email atau password belum sesuai. Periksa kembali lalu coba lagi."
        lower.hasAny("permission denied", "insufficient permission", "not authorized") -> "Akses admin belum tersedia untuk akun ini."
        lower.hasAny("storage", "object does not exist", "bucket", "upload", "file not found") && tone == StatusTone.Error -> "Foto belum bisa diunggah. Periksa koneksi dan konfigurasi penyimpanan."
        lower.hasAny("exception", "com.google", "java.", "kotlin.", "firebase", "firestore") -> "Terjadi kendala teknis. Coba lagi beberapa saat lagi."
        else -> cleanMessage
    }
}

private fun String.hasAny(vararg needles: String): Boolean = needles.any { contains(it) }
