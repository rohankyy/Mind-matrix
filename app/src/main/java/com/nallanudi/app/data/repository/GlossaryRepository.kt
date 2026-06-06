package com.nallanudi.app.data.repository

import com.nallanudi.app.data.db.EntryDao
import com.nallanudi.app.data.db.WordOfDayHistoryDao
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.model.Subject
import com.nallanudi.app.data.model.WordOfDayHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for glossary entries and Word of the Day.
 * All operations are local — no network calls.
 */
class GlossaryRepository(
    private val entryDao: EntryDao,
    private val wordOfDayHistoryDao: WordOfDayHistoryDao
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE  // "YYYY-MM-DD"

    /**
     * Search entries by query string, optionally filtered by subject.
     * Uses Room FTS4 for sub-200ms performance (Requirement 1.1, 1.2).
     */
    fun search(query: String, subject: Subject? = null): Flow<List<Entry>> {
        return if (subject != null) {
            entryDao.search(query).map { list ->
                list.filter { it.subject == subject }
            }
        } else {
            entryDao.search(query)
        }
    }

    /**
     * Get all entries for a specific subject (Requirement 2.2).
     */
    fun getBySubject(subject: Subject): Flow<List<Entry>> =
        entryDao.getBySubject(subject.name)

    /**
     * Count entries for a specific subject (Requirement 2.5).
     */
    suspend fun countBySubject(subject: Subject): Int =
        entryDao.countBySubject(subject.name)

    /**
     * Get a single entry by ID.
     */
    suspend fun getById(id: Long): Entry? = entryDao.getById(id)

    /**
     * Word of the Day algorithm (Requirements 6.1, 6.2, 6.4):
     * 1. Check history for today's date — return cached entry if found.
     * 2. Compute unseen entries (all IDs minus already-shown IDs).
     * 3. If all entries have been shown, reset history (full cycle complete).
     * 4. Select deterministically using hash(date) % unseen.size.
     * 5. Persist selection and return entry.
     */
    suspend fun getWordOfDay(date: LocalDate): Entry? {
        val dateStr = date.format(dateFormatter)

        // Step 1: Check cache
        val cachedId = wordOfDayHistoryDao.getForDate(dateStr)
        if (cachedId != null) {
            return entryDao.getById(cachedId)
        }

        // Step 2: Compute unseen set
        val allIds = entryDao.getAllIds()
        if (allIds.isEmpty()) return null

        val shownIds = wordOfDayHistoryDao.getAllShownIds().toSet()
        var unseenIds = allIds.filter { it !in shownIds }

        // Step 3: Reset if full cycle complete
        if (unseenIds.isEmpty()) {
            wordOfDayHistoryDao.clearAll()
            unseenIds = allIds
        }

        // Step 4: Deterministic selection
        val dateHash = dateStr.hashCode()
        val index = Math.abs(dateHash) % unseenIds.size
        val selectedId = unseenIds[index]

        // Step 5: Persist and return
        wordOfDayHistoryDao.record(
            WordOfDayHistory(entryId = selectedId, dateShown = dateStr)
        )
        return entryDao.getById(selectedId)
    }
}
