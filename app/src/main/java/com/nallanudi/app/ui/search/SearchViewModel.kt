package com.nallanudi.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.model.Subject
import com.nallanudi.app.data.repository.GlossaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Search screen.
 * Combines query + subject filter to produce search results (Requirements 1.1, 1.2, 2.2–2.4).
 * Debounces query input by 150ms to stay within the 200ms latency budget.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val glossaryRepository: GlossaryRepository) : ViewModel() {

    val query = MutableStateFlow("")
    val activeSubject = MutableStateFlow<Subject?>(null)

    /**
     * Combined search results — empty list when no query and no filter active.
     * Debounced 150ms to stay within 200ms requirement (Requirement 1.1).
     */
    val results: StateFlow<List<Entry>> = combine(
        query.debounce(150),
        activeSubject
    ) { q, subject -> Pair(q, subject) }
        .flatMapLatest { (q, subject) ->
            if (q.isBlank() && subject == null) {
                // No query and no filter — return empty list (home screen state)
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else if (q.isBlank() && subject != null) {
                // Filter only — show all entries for subject
                glossaryRepository.getBySubject(subject)
            } else {
                glossaryRepository.search(q.trim(), subject)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setQuery(newQuery: String) {
        query.value = newQuery
    }

    fun setSubjectFilter(subject: Subject?) {
        activeSubject.value = subject
    }

    class Factory(private val repository: GlossaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}
