package com.nallanudi.app.ui.flashcard

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
import com.nallanudi.app.databinding.FragmentFlashcardBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Flashcard revision screen.
 * Requirements: 5.1–5.6
 */
class FlashcardFragment : Fragment() {

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashcardViewModel by viewModels {
        val app = requireActivity().application as NallaNudiApplication
        FlashcardViewModel.Factory(app.myListRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkCardCount()
        observeState()
        setupControls()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.cards,
                    viewModel.currentIndex,
                    viewModel.isFlipped,
                    viewModel.isComplete
                ) { cards, index, flipped, complete ->
                    FlashcardUiState(cards.size, index, flipped, complete,
                        if (cards.isNotEmpty() && index < cards.size) cards[index] else null)
                }.collect { state ->
                    if (state.isComplete) {
                        showCompletion()
                        return@collect
                    }

                    val entry = state.currentEntry ?: return@collect

                    // Progress indicator
                    binding.tvCardProgress.text = getString(
                        R.string.card_progress, state.currentIndex + 1, state.totalCards
                    )

                    // Bind term to both faces
                    binding.tvCardTerm.text = entry.term
                    binding.tvCardTermBack.text = entry.term
                    binding.tvCardTranslation.text = entry.kannadaTranslation
                    binding.tvCardExplanation.text = entry.kannadaExplanation

                    // Flip state (Requirements 5.1, 5.2)
                    if (state.isFlipped) {
                        binding.layoutCardFront.visibility = View.GONE
                        binding.layoutCardBack.visibility = View.VISIBLE
                    } else {
                        binding.layoutCardFront.visibility = View.VISIBLE
                        binding.layoutCardBack.visibility = View.GONE
                    }

                    // Previous button disabled at first card (Requirement 5.4)
                    binding.btnPrevious.isEnabled = state.currentIndex > 0

                    // Show card content, hide completion
                    binding.layoutCompletion.visibility = View.GONE
                    binding.cardFlashcard.visibility = View.VISIBLE
                    binding.btnPrevious.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showCompletion() {
        binding.cardFlashcard.visibility = View.GONE
        binding.btnPrevious.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.layoutCompletion.visibility = View.VISIBLE
    }

    private fun setupControls() {
        // Tap card to flip (Requirement 5.2)
        binding.cardFlashcard.setOnClickListener { viewModel.flip() }

        // Navigation (Requirements 5.3, 5.4)
        binding.btnNext.setOnClickListener { viewModel.next() }
        binding.btnPrevious.setOnClickListener { viewModel.previous() }

        // Restart (Requirement 5.5)
        binding.btnRestart.setOnClickListener { viewModel.restart() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class FlashcardUiState(
        val totalCards: Int,
        val currentIndex: Int,
        val isFlipped: Boolean,
        val isComplete: Boolean,
        val currentEntry: com.nallanudi.app.data.model.Entry?
    )
}
