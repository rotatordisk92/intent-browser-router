package com.example.intentbrowserrouter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.intentbrowserrouter.browser.BrowserManager
import com.example.intentbrowserrouter.data.Rule
import com.example.intentbrowserrouter.databinding.ActivityBrowserDetailsBinding
import com.example.intentbrowserrouter.storage.PreferencesManager
import com.example.intentbrowserrouter.ui.RuleAdapter
import com.example.intentbrowserrouter.ui.RuleEditDialog

class BrowserDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBrowserDetailsBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var browserManager: BrowserManager
    private lateinit var browserPackage: String
    private lateinit var browserName: String
    private val adapter = RuleAdapter(
        onEdit = { rule -> editRule(rule) },
        onDelete = { rule -> deleteRule(rule) },
        onReassign = { rule -> reassignRule(rule) },
        isDefaultBrowser = false,
        onSetDefault = { /* not used for regular browsers */ }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityBrowserDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            browserPackage = intent.getStringExtra("browser_package") ?: run {
                Log.e("BrowserDetailsActivity", "No browser_package provided")
                finish()
                return
            }
            
            browserName = intent.getStringExtra("browser_name") ?: "Unknown Browser"
            
            prefsManager = PreferencesManager(this)
            browserManager = BrowserManager(this)
            
            setupToolbar()
            setupRecyclerView()
            setupFab()
            loadRules()
            Log.d("BrowserDetailsActivity", "Initialized for $browserName")
        } catch (e: Exception) {
            Log.e("BrowserDetailsActivity", "Error in onCreate", e)
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = browserName
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        binding.ruleList.layoutManager = LinearLayoutManager(this)
        binding.ruleList.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddRule.setOnClickListener {
            RuleEditDialog.show(this, browserPackage, null) { rule ->
                saveRule(rule)
            }
        }
    }

    private fun loadRules() {
        val rules = prefsManager.getRules(browserPackage)
        adapter.submitList(rules)
    }

    private fun saveRule(rule: Rule) {
        val currentRules = prefsManager.getRules(browserPackage).toMutableList()
        
        // Remove if editing (by hostname pattern)
        currentRules.removeAll { it.hostPattern == rule.hostPattern && it != rule }
        
        // Add the rule
        if (!currentRules.contains(rule)) {
            currentRules.add(rule)
        }
        
        prefsManager.saveRules(browserPackage, currentRules)
        loadRules()
        Snackbar.make(binding.root, "Rule saved", Snackbar.LENGTH_SHORT).show()
    }

    private fun editRule(rule: Rule) {
        val originalPattern = rule.hostPattern
        RuleEditDialog.show(this, browserPackage, rule) { updatedRule ->
            val currentRules = prefsManager.getRules(browserPackage).toMutableList()
            currentRules.removeAll { it.hostPattern == originalPattern }
            currentRules.add(updatedRule)
            prefsManager.saveRules(browserPackage, currentRules)
            loadRules()
            Snackbar.make(binding.root, "Rule updated", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteRule(rule: Rule) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Rule")
            .setMessage("Delete rule for ${rule.hostPattern}?")
            .setPositiveButton("Delete") { _, _ ->
                val currentRules = prefsManager.getRules(browserPackage)
                    .filter { it.hostPattern != rule.hostPattern }
                prefsManager.saveRules(browserPackage, currentRules)
                loadRules()
                Snackbar.make(binding.root, "Rule deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun reassignRule(rule: Rule) {
        val currentRules = prefsManager.getRules(browserPackage)
        val ruleToReassign = currentRules.find { it.hostPattern == rule.hostPattern } ?: return
        
        val browsers = browserManager.getInstalledBrowsers()
        val browserNames = browsers.map { it.displayName }.toTypedArray()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Re-assign ${rule.hostPattern}")
            .setItems(browserNames) { _, which ->
                val newBrowser = browsers[which].packageName
                val newRules = currentRules.filter { it.hostPattern != rule.hostPattern }
                prefsManager.saveRules(browserPackage, newRules)
                
                val updatedRule = ruleToReassign.copy(browserPackage = newBrowser)
                val newBrowserRules = prefsManager.getRules(newBrowser).toMutableList()
                newBrowserRules.add(updatedRule)
                prefsManager.saveRules(newBrowser, newBrowserRules)
                
                loadRules()
                Snackbar.make(binding.root, "Rule re-assigned", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
