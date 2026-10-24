package com.akacaraka.herbal.ui

enum class AppPage {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTER,
    HOME,
    EXPLORE,
    FAVORITES,
    PROFILE,
    DETAIL,
    ADMIN,
    REPORT,
    REPORT_DETAIL
}

enum class UserSessionType {
    SIGNED_OUT,
    GUEST,
    ACCOUNT
}

data class UserSession(
    val type: UserSessionType = UserSessionType.SIGNED_OUT,
    val email: String = "",
    val userId: String = ""
)

enum class ReportType(val label: String) {
    TANAMAN("Laporan Tanaman Herbal"),
    MINUMAN("Laporan Minuman Herbal"),
    TAMU("Laporan Pengunjung Tamu"),
    STATISTIK("Audit & Statistik Data")
}
