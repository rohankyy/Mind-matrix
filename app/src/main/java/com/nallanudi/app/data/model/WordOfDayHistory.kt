package com.nallanudi.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_of_day_history",
    indices = [Index(value = ["date_shown"], unique = true)]
)
data class WordOfDayHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entry_id") val entryId: Long,
    @ColumnInfo(name = "date_shown") val dateShown: String  // ISO-8601: "YYYY-MM-DD"
)
