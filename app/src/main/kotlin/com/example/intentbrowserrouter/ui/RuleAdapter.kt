package com.example.intentbrowserrouter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.intentbrowserrouter.data.Rule
import com.example.intentbrowserrouter.databinding.ItemRuleBinding

class RuleAdapter(
    private val onEdit: (Rule) -> Unit,
    private val onDelete: (Rule) -> Unit,
    private val onReassign: (Rule) -> Unit,
    private val isDefaultBrowser: Boolean = false,
    private val onSetDefault: (String?) -> Unit = {}
) : ListAdapter<Rule, RuleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: Rule) {
            binding.hostPattern.text = rule.hostPattern
            
            if (rule.incognito) {
                binding.incognitoIndicator.visibility = View.VISIBLE
            } else {
                binding.incognitoIndicator.visibility = View.GONE
            }
            
            binding.editButton.setOnClickListener {
                onEdit(rule)
            }
            
            binding.deleteButton.setOnClickListener {
                onDelete(rule)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Rule>() {
        override fun areItemsTheSame(oldItem: Rule, newItem: Rule) =
            oldItem.hostPattern == newItem.hostPattern

        override fun areContentsTheSame(oldItem: Rule, newItem: Rule) =
            oldItem == newItem
    }
}
