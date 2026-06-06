package com.nallanudi.app.data.repository

import com.nallanudi.app.data.db.MyListDao
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.model.MyListEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for My List (saved difficult words).
 * All operations are local — no network calls (Requirement 7.4).
 */
class MyListRepository(private val myListDao: MyListDao) {

    /**
     * Get all saved entries, most recently saved first (Requirement 4.4).
     */
    fun getAll(): Flow<List<Entry>> = myListDao.getAll()

    /**
     * Observe whether a specific entry is saved (Requirement 4.3).
     */
    fun isSaved(entryId: Long): Flow<Boolean> = myListDao.isSaved(entryId)

    /**
     * Count of saved entries.
     */
    fun count(): Flow<Int> = myListDao.count()

    /**
     * Toggle save state — save if not saved, remove if already saved.
     * This is an involution: toggle(toggle(x)) == x (Property 5, Requirements 4.1, 4.2).
     */
    suspend fun toggle(entryId: Long) {
        val saved = myListDao.isSaved(entryId).first()
        if (saved) {
            myListDao.remove(entryId)
        } else {
            myListDao.save(MyListEntry(entryId = entryId))
        }
    }
}
