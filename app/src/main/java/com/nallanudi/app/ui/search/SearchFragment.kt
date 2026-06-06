package com.nallanudi.app.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nallanudi.app.NallaNudiApplication
import com.nallanudi.app.R
import com.nallanudi.app.data.model.Subject
import com.nallanudi.app.databinding.FragmentSearchBinding
import com.nallanudi.app.ui.mylist.MyListViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Search screen — full-text search with subject filters and results list.
 * Requirements: 1.1–1.4, 2.1–2.5
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        val app = requireActivity().application as NallaNudiApplication
        SearchViewModel.Factory(app.glossaryRepository)
    }

    private val myListViewModel: MyListViewModel by activityViewModels {
        val app = requireActivity().application as NallaNudiApplication
        MyListViewModel.Factory(app.myListRepository)
    }

    private lateinit var adapter: EntryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchInput()
        setupSubjectChips()
        observeResults()

        // Apply pre-selected subject from navigation args (Requirement 2.1)
        val preselectedSubject = arguments?.getString("preselectedSubject")
        preselectedSubject?.let { subjectName ->
            val subject = Subject.values().find { it.name == subjectName }
            if (subject != null) {
                viewModel.setSubjectFilter(subject)
                when (subject) {
                    Subject.SCIENCE -> binding.chipFilterScience.isChecked = true
                    Subject.MATHEMATICS -> binding.chipFilterMathematics.isChecked = true
                    Subject.COMMERCE -> binding.chipFilterCommerce.isChecked = true
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EntryListAdapter(
            onItemClick = { entry ->
                val bundle = Bundle().apply { putLong("entryId", entry.id) }
                findNavController().navigate(R.id.action_search_to_detail, bundle)
            },
            onSaveClick = { entry ->
                myListViewModel.toggle(entry.id)
            }
        )
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    private fun setupSearchInput() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setQuery(s?.toString() ?: "")
            }
        })
    }

    private fun setupSubjectChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val subject = when {
                checkedIds.contains(binding.chipFilterScience.id) -> Subject.SCIENCE
                checkedIds.contains(binding.chipFilterMathematics.id) -> Subject.MATHEMATICS
                checkedIds.contains(binding.chipFilterCommerce.id) -> Subject.COMMERCE
                else -> null
            }
            viewModel.setSubjectFilter(subject)
        }
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.results,
                    myListViewModel.entries
                ) { results, savedEntries ->
                    Pair(results, savedEntries.map { it.id }.toSet())
                }.collect { (results, savedIds) ->
                    val query = viewModel.query.value
                    val subject = viewModel.activeSubject.value

                    when {
                        query.isBlank() && subject == null -> {
                            binding.rvResults.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.GONE
                            binding.layoutSearchPrompt.visibility = View.VISIBLE
                            binding.tvResultCount.visibility = View.GONE
                        }
                        results.isEmpty() -> {
                            // No results found (Requirement 1.4)
                            binding.rvResults.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                            binding.layoutSearchPrompt.visibility = View.GONE
                            binding.tvResultCount.visibility = View.GONE
                        }
                        else -> {
                            binding.rvResults.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                            binding.layoutSearchPrompt.visibility = View.GONE

                            // Show count when filter is active (Requirement 2.5)
                            if (subject != null) {
                                binding.tvResultCount.text =
                                    "${results.size} terms in ${subject.name.lowercase()}"
                                binding.tvResultCount.visibility = View.VISIBLE
                            } else {
                                binding.tvResultCount.visibility = View.GONE
                            }

                            adapter.submitEntries(results, savedIds)
                        }
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
