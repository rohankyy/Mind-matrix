package com.nallanudi.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nallanudi.app.data.model.WordOfDayHistory

@Dao
interface WordOfDayHistoryDao {

    @Query("SELECT entry_id FROM word_of_day_history WHERE date_shown = :date LIMIT 1")
    suspend fun getForDate(date: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun record(history: WordOfDayHistory)

    @Query("SELECT entry_id FROM word_of_day_history ORDER BY date_shown ASC")
    suspend fun getAllShownIds(): List<Long>

    @Query("DELETE FROM word_of_day_history")
    suspend fun clearAll()
}
