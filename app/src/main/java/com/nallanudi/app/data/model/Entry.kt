package com.nallanudi.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nallanudi.app.data.db.SubjectConverter

@Entity(
    tableName = "entry",
    indices = [Index(value = ["term"], unique = true)]
)
@TypeConverters(SubjectConverter::class)
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "kannada_translation") val kannadaTranslation: String,
    @ColumnInfo(name = "kannada_explanation") val kannadaExplanation: String,
    @ColumnInfo(name = "example") val example: String,
    @ColumnInfo(name = "subject") val subject: Subject
)
