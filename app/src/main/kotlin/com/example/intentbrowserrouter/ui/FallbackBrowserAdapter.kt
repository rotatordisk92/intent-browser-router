package com.example.intentbrowserrouter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.intentbrowserrouter.data.BrowserInfo
import com.example.intentbrowserrouter.databinding.ItemFallbackBrowserBinding

class FallbackBrowserAdapter(
    private var selectedPackage: String?,
    private val onSelected: (String) -> Unit
) : ListAdapter<BrowserInfo, FallbackBrowserAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFallbackBrowserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelected(packageName: String?) {
        selectedPackage = packageName
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemFallbackBrowserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(browser: BrowserInfo) {
            val pm = binding.root.context.packageManager
            try {
                binding.browserIcon.setImageDrawable(pm.getApplicationIcon(browser.packageName))
            } catch (e: Exception) {
                // Fallback icon?
            }
            binding.browserName.text = browser.displayName
            binding.radioButton.isChecked = browser.packageName == selectedPackage

            binding.root.setOnClickListener {
                if (browser.packageName != selectedPackage) {
                    onSelected(browser.packageName)
                    selectedPackage = browser.packageName
                    notifyDataSetChanged()
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BrowserInfo>() {
        override fun areItemsTheSame(oldItem: BrowserInfo, newItem: BrowserInfo) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: BrowserInfo, newItem: BrowserInfo) =
            oldItem == newItem
    }
}
