package com.example.intentbrowserrouter.browser

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.example.intentbrowserrouter.data.BrowserInfo

class BrowserManager(private val context: Context) {
    fun getInstalledBrowsers(): List<BrowserInfo> {
        val browsers = mutableListOf<BrowserInfo>()
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://")
        }
        
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val browserPackages = resolveInfos.map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()
        
        for (packageName in browserPackages) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                browsers.add(BrowserInfo(packageName, label, true))
            } catch (e: Exception) {
                // Skip if unable to get app info
            }
        }
        
        return browsers.sortedBy { it.displayName }
    }

    fun isBrowserInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun supportsIncognito(browserPackage: String): Boolean {
        return browserPackage in FIREFOX_PACKAGES || browserPackage in CHROMIUM_PACKAGES
    }

    fun launchBrowser(url: String, browserPackage: String, incognito: Boolean = false) {
        val uri = android.net.Uri.parse(url)

        try {
            when {
                // Chromium: ACTION_VIEW extras are silently ignored; Custom Tabs ephemeral mode is required
                incognito && browserPackage in CHROMIUM_PACKAGES -> {
                    // Build the custom tabs intent with ephemeral mode enabled via the Builder method
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setEphemeralBrowsingEnabled(true)
                        .build()

                    // 2. Safely configure the underlying intent object without scope ambiguities
                    customTabsIntent.intent.setPackage(browserPackage)
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    customTabsIntent.launchUrl(context, uri)
                }
                else -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(browserPackage)
                        if (incognito && browserPackage in FIREFOX_PACKAGES) {
                            putExtra(EXTRA_FIREFOX_PRIVATE, true)
                        }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
            return
        } catch (e: ActivityNotFoundException) {
            if (incognito) Toast.makeText(context, "❌ Incognito launch failed", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            if (incognito) Toast.makeText(context, "❌ Incognito launch failed", Toast.LENGTH_SHORT).show()
        }

        // Fallback: same package, no incognito
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(browserPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            return
        } catch (e: ActivityNotFoundException) { /* fall through */ }

        // Last resort: no package restriction
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) { /* silent fail */ }
    }

    companion object {
        // Firefox family: private browsing via "private_browsing_mode" extra
        val FIREFOX_PACKAGES = setOf(
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.fenix",
            "org.mozilla.fenix.debug",
            "org.mozilla.fennec_fdroid",
            "org.mozilla.fennec_aurora",
            "org.mozilla.fennec",
            "org.mozilla.rocket",
            "io.github.forkmaintainers.iceraven",
            "us.spotco.fennec_dos",
            "org.torproject.torbrowser",
            "org.torproject.torbrowser_alpha",
            "org.mozilla.reference.browser",
            "info.guardianproject.orfox",
            "org.ironfoxoss.ironfox",
            "org.ironfoxoss.ironfox.nightly"
        )

        // Chromium family: ACTION_VIEW extras don't work; use CustomTabsIntent ephemeral mode
        val CHROMIUM_PACKAGES = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.google.android.apps.chrome",
            "org.chromium.chrome",
            "com.brave.browser",
            "com.brave.browser_beta",
            "com.brave.browser_nightly",
            "com.microsoft.emmx",
            "com.vivaldi.browser",
            "com.vivaldi.browser.snapshot",
            "com.kiwibrowser.browser",
            "com.kiwibrowser.browser.dev",
            "com.sec.android.app.sbrowser",
            "com.sec.android.app.sbrowser.beta",
            "com.opera.browser",
            "com.opera.browser.beta",
            "com.opera.gx",
            "com.opera.cryptobrowser",
            "com.ucmobile.intl",
            "com.uc.browser.en",
            "org.cromite.cromite",
            "us.spotco.mulch",
            "org.chromium.thorium",
            "app.vanadium.browser"
        )

        private const val EXTRA_FIREFOX_PRIVATE = "private_browsing_mode"
    }
}
