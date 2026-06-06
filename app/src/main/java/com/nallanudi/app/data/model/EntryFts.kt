package com.nallanudi.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = Entry::class)
@Entity(tableName = "entry_fts")
data class EntryFts(
    @ColumnInfo(name = "term") val term: String
)
