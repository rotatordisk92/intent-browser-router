package com.example.intentbrowserrouter.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.intentbrowserrouter.data.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "intent_browser_router",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveRules(browserPackage: String, rules: List<Rule>) {
        val key = "rules_$browserPackage"
        val json = gson.toJson(rules)
        prefs.edit().putString(key, json).apply()
    }

    fun getRules(browserPackage: String): List<Rule> {
        val key = "rules_$browserPackage"
        val json = prefs.getString(key, "[]") ?: "[]"
        val type = object : TypeToken<List<Rule>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setDefaultBrowser(packageName: String) {
        prefs.edit().putString("default_browser", packageName).apply()
    }

    fun getDefaultBrowser(): String? {
        return prefs.getString("default_browser", null)
    }

    fun deleteRulesForBrowser(browserPackage: String) {
        val key = "rules_$browserPackage"
        prefs.edit().remove(key).apply()
    }

    fun exportToJson(): String {
        val allRules = mutableMapOf<String, List<Rule>>()
        val allBrowserRules = prefs.all.filter { it.key.startsWith("rules_") }
        for ((key, value) in allBrowserRules) {
            val browserPackage = key.removePrefix("rules_")
            if (value is String) {
                val type = object : TypeToken<List<Rule>>() {}.type
                val rules: List<Rule> = gson.fromJson(value, type)
                allRules[browserPackage] = rules
            }
        }
        val data = mapOf(
            "rules" to allRules,
            "defaultBrowser" to (getDefaultBrowser() ?: "")
        )
        return gson.toJson(data)
    }

    fun importFromJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, type)
            
            @Suppress("UNCHECKED_CAST")
            val rules = data["rules"] as? Map<String, List<Map<String, Any>>> ?: return false
            val defaultBrowser = data["defaultBrowser"] as? String
            
            val edit = prefs.edit()
            
            // Clear existing rules
            prefs.all.filter { it.key.startsWith("rules_") }
                .forEach { edit.remove(it.key) }
            
            // Import rules
            for ((browserPackage, rulesList) in rules) {
                val rulesJson = gson.toJson(rulesList)
                edit.putString("rules_$browserPackage", rulesJson)
            }
            
            // Import default browser
            if (!defaultBrowser.isNullOrEmpty()) {
                edit.putString("default_browser", defaultBrowser)
            }
            
            edit.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllBrowserRules(): Map<String, List<Rule>> {
        val result = mutableMapOf<String, List<Rule>>()
        val allRules = prefs.all.filter { it.key.startsWith("rules_") }
        for ((key, value) in allRules) {
            val browserPackage = key.removePrefix("rules_")
            if (value is String) {
                val type = object : TypeToken<List<Rule>>() {}.type
                val rules: List<Rule> = gson.fromJson(value, type)
                result[browserPackage] = rules
            }
        }
        return result
    }
}
