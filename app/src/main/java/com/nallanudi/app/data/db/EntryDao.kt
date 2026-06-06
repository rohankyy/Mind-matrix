package com.nallanudi.app.data.db

import androidx.room.Dao
import androidx.room.Query
import com.nallanudi.app.data.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    // FTS search — prefix match using MATCH operator
    @Query("SELECT e.* FROM entry e JOIN entry_fts fts ON e.id = fts.rowid WHERE entry_fts MATCH :query || '*' ORDER BY e.term ASC")
    fun search(query: String): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE subject = :subject ORDER BY term ASC")
    fun getBySubject(subject: String): Flow<List<Entry>>

    @Query("SELECT COUNT(*) FROM entry WHERE subject = :subject")
    suspend fun countBySubject(subject: String): Int

    @Query("SELECT * FROM entry WHERE id = :id")
    suspend fun getById(id: Long): Entry?

    @Query("SELECT COUNT(*) FROM entry")
    suspend fun totalCount(): Int

    @Query("SELECT id FROM entry ORDER BY id ASC")
    suspend fun getAllIds(): List<Long>
}
