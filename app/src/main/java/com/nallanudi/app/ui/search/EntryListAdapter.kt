package com.nallanudi.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nallanudi.app.R
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.databinding.ItemEntryBinding

/**
 * RecyclerView adapter for displaying glossary entries.
 * Uses DiffUtil for efficient updates (Requirements 1.3, 4.3).
 */
class EntryListAdapter(
    private val onItemClick: (Entry) -> Unit,
    private val onSaveClick: (Entry) -> Unit
) : ListAdapter<EntryListAdapter.EntryItem, EntryListAdapter.ViewHolder>(DiffCallback()) {

    /**
     * Wraps an Entry with its saved state for display.
     */
    data class EntryItem(val entry: Entry, val isSaved: Boolean)

    fun submitEntries(entries: List<Entry>, savedIds: Set<Long>) {
        submitList(entries.map { EntryItem(it, it.id in savedIds) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EntryItem) {
            val entry = item.entry
            binding.tvTerm.text = entry.term
            binding.tvKannadaTranslation.text = entry.kannadaTranslation
            binding.tvSubjectBadge.text = entry.subject.name.lowercase()
                .replaceFirstChar { it.uppercase() }

            // Saved state visual distinction (Requirement 4.3)
            binding.btnSave.setImageResource(
                if (item.isSaved) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.btnSave.contentDescription = if (item.isSaved) "Saved" else "Save word"

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.btnSave.setOnClickListener { onSaveClick(entry) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<EntryItem>() {
        override fun areItemsTheSame(oldItem: EntryItem, newItem: EntryItem): Boolean =
            oldItem.entry.id == newItem.entry.id

        override fun areContentsTheSame(oldItem: EntryItem, newItem: EntryItem): Boolean =
            oldItem == newItem
    }
}
