package com.nallanudi.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nallanudi.app.NallaNudiApplication
import com.nallanudi.app.R
import com.nallanudi.app.data.model.Subject
import com.nallanudi.app.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

/**
 * Home screen — shows Word of the Day and subject filter shortcuts.
 * Requirements: 6.1, 6.3, 2.1, 2.3
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as NallaNudiApplication
        HomeViewModel.Factory(app.glossaryRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe Word of the Day
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wordOfDay.collect { entry ->
                    if (entry != null) {
                        binding.tvWotdTerm.text = entry.term
                        binding.tvWotdTranslation.text = entry.kannadaTranslation
                    }
                }
            }
        }

        // Tap Word of the Day card → navigate to detail (Requirement 6.3)
        binding.cardWordOfDay.setOnClickListener {
            val entry = viewModel.wordOfDay.value ?: return@setOnClickListener
            // Navigate using bundle args directly (no Safe Args needed for simple long)
            val bundle = android.os.Bundle().apply {
                putLong("entryId", entry.id)
            }
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }

        // Subject filter chips → navigate to search with pre-selected subject
        binding.chipScience.setOnClickListener {
            navigateToSearchWithSubject(Subject.SCIENCE)
        }
        binding.chipMathematics.setOnClickListener {
            navigateToSearchWithSubject(Subject.MATHEMATICS)
        }
        binding.chipCommerce.setOnClickListener {
            navigateToSearchWithSubject(Subject.COMMERCE)
        }

        // Search shortcut button
        binding.btnSearchShortcut.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_search)
        }
    }

    private fun navigateToSearchWithSubject(subject: Subject) {
        val bundle = android.os.Bundle().apply {
            putString("preselectedSubject", subject.name)
        }
        findNavController().navigate(R.id.action_home_to_search, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
