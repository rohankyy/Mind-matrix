package com.nallanudi.app.data.db

import androidx.room.TypeConverter
import com.nallanudi.app.data.model.Subject

class SubjectConverter {

    @TypeConverter
    fun fromSubject(subject: Subject): String = subject.name

    @TypeConverter
    fun toSubject(value: String): Subject = Subject.valueOf(value)
}
