package com.nallanudi.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nallanudi.app.NallaNudiApplication
import com.nallanudi.app.R
import com.nallanudi.app.databinding.FragmentEntryDetailBinding
import com.nallanudi.app.util.TtsManager
import kotlinx.coroutines.launch

/**
 * Entry Detail screen — shows full entry with TTS pronunciation and save toggle.
 * Requirements: 1.3, 3.1–3.4, 4.1–4.3
 *
 * Receives argument: entryId (Long) via Bundle — set in nav_graph.xml
 */
class EntryDetailFragment : Fragment() {

    private var _binding: FragmentEntryDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryDetailViewModel by viewModels {
        val app = requireActivity().application as NallaNudiApplication
        EntryDetailViewModel.Factory(
            app.glossaryRepository,
            app.myListRepository,
            TtsManager(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read entryId from Bundle (passed via navigate(resId, bundle))
        val entryId = arguments?.getLong("entryId", -1L) ?: -1L
        viewModel.loadEntry(entryId)

        observeEntry()
        observeTtsState()
        observeSaveState()

        binding.btnPronounce.setOnClickListener { viewModel.speak() }
        binding.btnSaveDetail.setOnClickListener { viewModel.toggleSave() }
    }

    private fun observeEntry() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entry.collect { entry ->
                    if (entry != null) {
                        binding.tvDetailTerm.text = entry.term
                        binding.tvDetailSubject.text = entry.subject.name.lowercase()
                            .replaceFirstChar { it.uppercase() }
                        binding.tvDetailTranslation.text = entry.kannadaTranslation
                        binding.tvDetailExplanation.text = entry.kannadaExplanation
                        binding.tvDetailExample.text = entry.example
                        requireActivity().title = entry.term
                    }
                }
            }
        }
    }

    private fun observeTtsState() {
        // TTS availability (Requirement 3.2)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ttsAvailable.collect { available ->
                    binding.btnPronounce.isEnabled = available
                    binding.btnPronounce.alpha = if (available) 1.0f else 0.4f
                }
            }
        }

        // Speaking indicator (Requirements 3.3, 3.4)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSpeaking.collect { speaking ->
                    if (speaking) {
                        binding.btnPronounce.setIconResource(android.R.drawable.ic_media_pause)
                        binding.btnPronounce.text = "Speaking…"
                    } else {
                        binding.btnPronounce.setIconResource(android.R.drawable.ic_lock_silent_mode_off)
                        binding.btnPronounce.text = getString(R.string.pronounce)
                    }
                }
            }
        }
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSaved.collect { saved ->
                    if (saved) {
                        binding.btnSaveDetail.setIconResource(android.R.drawable.btn_star_big_on)
                        binding.btnSaveDetail.text = getString(R.string.unsave_word)
                    } else {
                        binding.btnSaveDetail.setIconResource(android.R.drawable.btn_star_big_off)
                        binding.btnSaveDetail.text = getString(R.string.save_word)
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
