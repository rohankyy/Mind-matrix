package com.nallanudi.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.repository.GlossaryRepository
import com.nallanudi.app.data.repository.MyListRepository
import com.nallanudi.app.util.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Entry Detail screen.
 * Manages entry data, save state, and TTS pronunciation (Requirements 3.1–3.4, 4.1–4.3).
 */
class EntryDetailViewModel(
    private val glossaryRepository: GlossaryRepository,
    private val myListRepository: MyListRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _entryId = MutableStateFlow<Long>(-1L)

    private val _entry = MutableStateFlow<Entry?>(null)
    val entry: StateFlow<Entry?> = _entry.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isSaved: StateFlow<Boolean> = _entryId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(false)
            else myListRepository.isSaved(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val ttsAvailable: StateFlow<Boolean> = ttsManager.isAvailable
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun loadEntry(entryId: Long) {
        _entryId.value = entryId
        viewModelScope.launch {
            _entry.value = glossaryRepository.getById(entryId)
        }
    }

    /**
     * Toggle save state for the current entry (Requirements 4.1, 4.2).
     */
    fun toggleSave() {
        val id = _entryId.value
        if (id == -1L) return
        viewModelScope.launch {
            myListRepository.toggle(id)
        }
    }

    /**
     * Speak the current entry's English term (Requirement 3.1).
     */
    fun speak() {
        val term = _entry.value?.term ?: return
        ttsManager.speak(term)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    class Factory(
        private val glossaryRepository: GlossaryRepository,
        private val myListRepository: MyListRepository,
        private val ttsManager: TtsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EntryDetailViewModel(glossaryRepository, myListRepository, ttsManager) as T
        }
    }
}
