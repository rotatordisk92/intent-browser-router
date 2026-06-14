package com.example.intentbrowserrouter.ui

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.intentbrowserrouter.browser.BrowserManager
import com.example.intentbrowserrouter.data.Rule

object RuleEditDialog {
    fun show(
        context: Context,
        currentBrowserPackage: String,
        existingRule: Rule? = null,
        onSave: (Rule) -> Unit
    ) {
        val browserManager = BrowserManager(context)
        val supportsIncognito = browserManager.supportsIncognito(currentBrowserPackage)

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * context.resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val textInputLayout = TextInputLayout(context).apply {
            hint = "Hostname Pattern (e.g. *.google.com)"
            helperText = "Use * for wildcards"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val hostPatternInput = TextInputEditText(textInputLayout.context).apply {
            setText(existingRule?.hostPattern ?: "")
            setSingleLine(true)
            textSize = 18f
        }
        textInputLayout.addView(hostPatternInput)
        container.addView(textInputLayout)

        val incognitoCheckbox = CheckBox(context).apply {
            text = "Launch in Incognito Mode"
            isChecked = existingRule?.incognito ?: false
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = (24 * context.resources.displayMetrics.density).toInt()
            layoutParams = params
            textSize = 18f
            visibility = if (supportsIncognito) View.VISIBLE else View.GONE
        }
        container.addView(incognitoCheckbox)

        MaterialAlertDialogBuilder(context)
            .setTitle(if (existingRule != null) "Edit Rule" else "Add Rule")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val hostPattern = hostPatternInput.text?.toString()?.trim()
                if (!hostPattern.isNullOrEmpty()) {
                    val isIncognito = if (supportsIncognito) incognitoCheckbox.isChecked else false
                    onSave(Rule(hostPattern, currentBrowserPackage, isIncognito))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
