package com.nallanudi.app.ui.mylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.repository.MyListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the My List screen.
 * Exposes saved entries and count; handles save/remove toggle (Requirements 4.1–4.5).
 */
class MyListViewModel(private val myListRepository: MyListRepository) : ViewModel() {

    val entries: StateFlow<List<Entry>> = myListRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val count: StateFlow<Int> = myListRepository.count()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Toggle save state for an entry (Requirement 4.1, 4.2).
     */
    fun toggle(entryId: Long) {
        viewModelScope.launch {
            myListRepository.toggle(entryId)
        }
    }

    class Factory(private val repository: MyListRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyListViewModel(repository) as T
        }
    }
}
