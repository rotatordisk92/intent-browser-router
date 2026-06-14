package com.example.intentbrowserrouter.router

import android.net.Uri
import com.example.intentbrowserrouter.data.Rule

class UrlMatcher {
    fun matchesPattern(url: String, pattern: String): Boolean {
        val host = try {
            Uri.parse(url).host ?: return false
        } catch (e: Exception) {
            return false
        }
        
        return when {
            pattern == host -> true
            pattern.startsWith("*.") -> {
                val suffix = pattern.substring(2)
                host == suffix || host.endsWith(".$suffix")
            }
            pattern.endsWith(".*") -> {
                val prefix = pattern.substring(0, pattern.length - 2)
                host == prefix || host.startsWith("$prefix.")
            }
            pattern.contains("*") -> {
                val regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .let { "^$it$" }
                    .toRegex()
                regex.matches(host)
            }
            else -> host == pattern
        }
    }

    fun findMatchingRule(url: String, rules: List<Rule>): Rule? {
        return rules.find { matchesPattern(url, it.hostPattern) }
    }
}
