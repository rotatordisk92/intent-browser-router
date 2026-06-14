package com.example.intentbrowserrouter

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intentbrowserrouter.browser.BrowserManager
import com.example.intentbrowserrouter.data.BrowserInfo
import com.example.intentbrowserrouter.databinding.ActivityMainBinding
import com.example.intentbrowserrouter.storage.PreferencesManager
import com.example.intentbrowserrouter.ui.BrowserAdapter
import com.example.intentbrowserrouter.ui.FallbackBrowserAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var browserManager: BrowserManager
    
    private val browserAdapter = BrowserAdapter { browser -> onBrowserClick(browser) }
    private lateinit var fallbackAdapter: FallbackBrowserAdapter

    private val roleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updateDefaultBrowserStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            prefsManager = PreferencesManager(this)
            browserManager = BrowserManager(this)
            
            setupToolbar()
            setupRecyclerViews()
            setupStatusButton()
            loadData()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDefaultBrowserStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Intent Router"
    }

    private fun setupRecyclerViews() {
        binding.browserList.layoutManager = LinearLayoutManager(this)
        binding.browserList.adapter = browserAdapter

        fallbackAdapter = FallbackBrowserAdapter(prefsManager.getDefaultBrowser()) { pkg ->
            prefsManager.setDefaultBrowser(pkg)
            Snackbar.make(binding.root, "Fallback browser updated", Snackbar.LENGTH_SHORT).show()
        }
        binding.fallbackBrowserList.layoutManager = LinearLayoutManager(this)
        binding.fallbackBrowserList.adapter = fallbackAdapter
    }

    private fun setupStatusButton() {
        binding.btnDefaultStatus.setOnClickListener {
            requestDefaultBrowserRole()
        }
    }

    private fun loadData() {
        val browsers = browserManager.getInstalledBrowsers()
        
        if (browsers.isEmpty()) {
            binding.sectionCustomRules.visibility = android.view.View.GONE
            binding.sectionFallback.visibility = android.view.View.GONE
            Snackbar.make(binding.root, "No browsers detected! Ensure you have browsers installed.", Snackbar.LENGTH_LONG).show()
        } else {
            binding.sectionCustomRules.visibility = android.view.View.VISIBLE
            binding.sectionFallback.visibility = android.view.View.VISIBLE
            browserAdapter.submitList(browsers)
            fallbackAdapter.submitList(browsers)
        }

        updateDefaultBrowserStatus()
    }

    private fun updateDefaultBrowserStatus() {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isDefault = roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)

        if (isDefault) {
            binding.btnDefaultStatus.text = "Router is Active"
            binding.btnDefaultStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            binding.btnDefaultStatus.text = "Set as Default Browser"
            binding.btnDefaultStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
        }
        binding.btnDefaultStatus.setTextColor(Color.WHITE)
        binding.btnDefaultStatus.iconTint = ColorStateList.valueOf(Color.WHITE)
    }

    private fun requestDefaultBrowserRole() {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (!roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
            roleLauncher.launch(intent)
        } else {
            Snackbar.make(binding.root, "App is already the default browser", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onBrowserClick(browser: BrowserInfo) {
        val intent = Intent(this, BrowserDetailsActivity::class.java).apply {
            putExtra("browser_package", browser.packageName)
            putExtra("browser_name", browser.displayName)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportRules()
                true
            }
            R.id.action_import -> {
                importRules()
                true
            }
            R.id.action_app_links_info -> {
                showAppLinksInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportRules() {
        try {
            val json = prefsManager.exportToJson()
            val downloadsDir = File(getExternalFilesDir(null), "IntentBrowserRouter")
            downloadsDir.mkdirs()
            val file = File(downloadsDir, "browser_rules_${System.currentTimeMillis()}.json")
            file.writeText(json)
            Snackbar.make(binding.root, "Rules exported to ${file.absolutePath}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Export failed: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun importRules() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Import Rules")
            .setMessage("Place your browser_rules.json in the app's data directory and restart.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showAppLinksInfo() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About App Links")
            .setMessage("Apps with verified App Links will always open in their assigned app. This router cannot override those links.")
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    startActivity(Intent("android.intent.action.MANAGE_APP_LINKS"))
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Cannot open settings", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close") { _, _ -> }
            .show()
    }
}
