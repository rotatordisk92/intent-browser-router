package com.example.intentbrowserrouter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.intentbrowserrouter.data.BrowserInfo
import com.example.intentbrowserrouter.databinding.ItemBrowserBinding

class BrowserAdapter(
    private val onBrowserClick: (BrowserInfo) -> Unit
) : ListAdapter<BrowserInfo, BrowserAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBrowserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBrowserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(browser: BrowserInfo) {
            val pm = binding.root.context.packageManager
            try {
                binding.browserIcon.setImageDrawable(pm.getApplicationIcon(browser.packageName))
            } catch (e: Exception) {
                // Use a default icon
            }
            binding.browserName.text = browser.displayName
            binding.browserPackage.text = browser.packageName

            binding.root.setOnClickListener {
                onBrowserClick(browser)
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
