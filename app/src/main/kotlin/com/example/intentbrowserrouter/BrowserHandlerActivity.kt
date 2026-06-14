package com.example.intentbrowserrouter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.widget.Toast
import com.example.intentbrowserrouter.browser.BrowserManager
import com.example.intentbrowserrouter.router.UrlMatcher
import com.example.intentbrowserrouter.storage.PreferencesManager

class BrowserHandlerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val url = intent.data?.toString() ?: run {
                Log.d("BrowserHandlerActivity", "No URL provided, launching MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
            
            Log.d("BrowserHandlerActivity", "Handling URL: $url")
            
            val prefsManager = PreferencesManager(this)
            val browserManager = BrowserManager(this)
            val urlMatcher = UrlMatcher()
            
            // Get all rules from all browsers
            val allRules = prefsManager.getAllBrowserRules()
            var matchedRule: Pair<String, com.example.intentbrowserrouter.data.Rule>? = null
            
            // Find matching rule
            for ((browserPackage, rules) in allRules) {
                val rule = urlMatcher.findMatchingRule(url, rules)
                if (rule != null) {
                    Log.d("BrowserHandlerActivity", "Matched rule: $browserPackage for pattern ${rule.hostPattern}")
                    matchedRule = browserPackage to rule
                    break
                }
            }
            
            val targetBrowser = matchedRule?.first ?: prefsManager.getDefaultBrowser()
            
            if (targetBrowser != null && browserManager.isBrowserInstalled(targetBrowser)) {
                Log.d("BrowserHandlerActivity", "Launching $targetBrowser")
                
                // Show toast only if a match was found (not for catch-all)
                if (matchedRule != null) {
                    try {
                        val pm = packageManager
                        val appInfo = pm.getApplicationInfo(targetBrowser, 0)
                        val label = pm.getApplicationLabel(appInfo).toString()
                        // Use applicationContext to ensure toast shows even if activity finishes quickly
                        Toast.makeText(applicationContext, "Opening with $label", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "Opening with matched browser", Toast.LENGTH_SHORT).show()
                    }
                }

                browserManager.launchBrowser(
                    url,
                    targetBrowser,
                    matchedRule?.second?.incognito ?: false
                )
            } else {
                Log.d("BrowserHandlerActivity", "No target browser, showing chooser")
                // Fallback: show chooser
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = intent.data
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    startActivity(Intent.createChooser(fallbackIntent, "Open with"))
                } catch (e: Exception) {
                    Log.e("BrowserHandlerActivity", "Error showing chooser", e)
                }
            }
        } catch (e: Exception) {
            Log.e("BrowserHandlerActivity", "Fatal error in onCreate", e)
        }
        
        finish()
    }
}
