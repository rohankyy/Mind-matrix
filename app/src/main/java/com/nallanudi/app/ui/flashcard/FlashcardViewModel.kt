package com.nallanudi.app.ui.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.repository.MyListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Flashcard revision screen.
 * Manages card navigation, flip state, and completion (Requirements 5.1–5.6).
 */
class FlashcardViewModel(myListRepository: MyListRepository) : ViewModel() {

    val cards: StateFlow<List<Entry>> = myListRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    private val _tooFewCards = MutableStateFlow(false)
    val tooFewCards: StateFlow<Boolean> = _tooFewCards.asStateFlow()

    /**
     * Flip the current card to reveal/hide the back (Requirement 5.2).
     */
    fun flip() {
        _isFlipped.value = !_isFlipped.value
    }

    /**
     * Advance to the next card (Requirement 5.3).
     * Shows completion screen when reaching the last card.
     */
    fun next() {
        val size = cards.value.size
        if (size < 2) {
            _tooFewCards.value = true
            return
        }
        val nextIndex = _currentIndex.value + 1
        if (nextIndex >= size) {
            _isComplete.value = true
        } else {
            _currentIndex.value = nextIndex
            _isFlipped.value = false
        }
    }

    /**
     * Go back to the previous card (Requirement 5.4).
     */
    fun previous() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _isFlipped.value = false
        }
    }

    /**
     * Restart the flashcard session from the beginning (Requirement 5.5).
     */
    fun restart() {
        _currentIndex.value = 0
        _isFlipped.value = false
        _isComplete.value = false
    }

    /**
     * Check if there are enough cards to start a session (Requirement 5.6).
     */
    fun checkCardCount() {
        _tooFewCards.value = cards.value.size < 2
    }

    class Factory(private val repository: MyListRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardViewModel(repository) as T
        }
    }
}
