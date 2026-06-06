package com.nallanudi.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.repository.GlossaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for the Home screen.
 * Loads the Word of the Day entry (Requirements 6.1, 6.3).
 */
class HomeViewModel(private val glossaryRepository: GlossaryRepository) : ViewModel() {

    private val _wordOfDay = MutableStateFlow<Entry?>(null)
    val wordOfDay: StateFlow<Entry?> = _wordOfDay.asStateFlow()

    init {
        loadWordOfDay()
    }

    private fun loadWordOfDay() {
        viewModelScope.launch {
            _wordOfDay.value = glossaryRepository.getWordOfDay(LocalDate.now())
        }
    }

    class Factory(private val repository: GlossaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
