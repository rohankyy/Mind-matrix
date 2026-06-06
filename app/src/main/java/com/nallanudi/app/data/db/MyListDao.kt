package com.nallanudi.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.model.MyListEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MyListDao {

    @Query("SELECT e.* FROM entry e INNER JOIN my_list ml ON e.id = ml.entry_id ORDER BY ml.saved_at DESC")
    fun getAll(): Flow<List<Entry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(item: MyListEntry): Long

    @Query("DELETE FROM my_list WHERE entry_id = :entryId")
    suspend fun remove(entryId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM my_list WHERE entry_id = :entryId)")
    fun isSaved(entryId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM my_list")
    fun count(): Flow<Int>
}
