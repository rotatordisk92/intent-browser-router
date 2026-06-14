package com.example.intentbrowserrouter.data

data class Rule(
    val hostPattern: String,
    val browserPackage: String,
    val incognito: Boolean = false
)

data class BrowserInfo(
    val packageName: String,
    val displayName: String,
    val isInstalled: Boolean = true
)
