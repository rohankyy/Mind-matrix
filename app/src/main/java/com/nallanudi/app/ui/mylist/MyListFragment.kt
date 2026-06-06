package com.nallanudi.app.ui.mylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nallanudi.app.NallaNudiApplication
import com.nallanudi.app.R
import com.nallanudi.app.databinding.FragmentMyListBinding
import com.nallanudi.app.ui.search.EntryListAdapter
import kotlinx.coroutines.launch

/**
 * My List screen — shows saved entries and provides flashcard access.
 * Requirements: 4.4, 4.5, 5.6
 */
class MyListFragment : Fragment() {

    private var _binding: FragmentMyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyListViewModel by activityViewModels {
        val app = requireActivity().application as NallaNudiApplication
        MyListViewModel.Factory(app.myListRepository)
    }

    private lateinit var adapter: EntryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeEntries()

        binding.btnStartFlashcards.setOnClickListener {
            if (viewModel.count.value < 2) {
                // Requirement 5.6 — need at least 2 words
                Snackbar.make(
                    binding.root,
                    getString(R.string.too_few_cards) + "\n" + getString(R.string.too_few_cards_kannada),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                findNavController().navigate(R.id.action_mylist_to_flashcard)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EntryListAdapter(
            onItemClick = { entry ->
                val bundle = Bundle().apply { putLong("entryId", entry.id) }
                findNavController().navigate(R.id.action_mylist_to_detail, bundle)
            },
            onSaveClick = { entry ->
                viewModel.toggle(entry.id)
            }
        )
        binding.rvMyList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyList.adapter = adapter
    }

    private fun observeEntries() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { entries ->
                    if (entries.isEmpty()) {
                        binding.rvMyList.visibility = View.GONE
                        binding.layoutMyListEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvMyList.visibility = View.VISIBLE
                        binding.layoutMyListEmpty.visibility = View.GONE
                        adapter.submitEntries(entries, entries.map { it.id }.toSet())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
