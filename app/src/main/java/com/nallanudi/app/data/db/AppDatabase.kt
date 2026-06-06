package com.nallanudi.app.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nallanudi.app.data.model.Entry
import com.nallanudi.app.data.model.EntryFts
import com.nallanudi.app.data.model.MyListEntry
import com.nallanudi.app.data.model.Subject
import com.nallanudi.app.data.model.WordOfDayHistory

@Database(
    entities = [Entry::class, MyListEntry::class, WordOfDayHistory::class, EntryFts::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(SubjectConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun myListDao(): MyListDao
    abstract fun wordOfDayHistoryDao(): WordOfDayHistoryDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DB_NAME = "nalla_nudi.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun build(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .createFromAsset("database/nalla_nudi.db")
                    .addCallback(validationCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Validates all entries on first database creation.
         * Uses SupportSQLiteDatabase directly (Room DAOs are not available during onCreate).
         * Logs and deletes any entry where required text fields are empty/blank,
         * or where the subject value is not a valid Subject enum member.
         * Requirement 8.2, 8.3
         */
        private val validationCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                val validSubjects = Subject.entries.map { it.name }.toSet()
                val idsToDelete = mutableListOf<Long>()

                val cursor = db.query(
                    "SELECT id, term, kannada_translation, kannada_explanation, example, subject FROM entry"
                )
                cursor.use {
                    val idxId = it.getColumnIndexOrThrow("id")
                    val idxTerm = it.getColumnIndexOrThrow("term")
                    val idxKannadaTranslation = it.getColumnIndexOrThrow("kannada_translation")
                    val idxKannadaExplanation = it.getColumnIndexOrThrow("kannada_explanation")
                    val idxExample = it.getColumnIndexOrThrow("example")
                    val idxSubject = it.getColumnIndexOrThrow("subject")

                    while (it.moveToNext()) {
                        val id = it.getLong(idxId)
                        val term = it.getString(idxTerm) ?: ""
                        val kannadaTranslation = it.getString(idxKannadaTranslation) ?: ""
                        val kannadaExplanation = it.getString(idxKannadaExplanation) ?: ""
                        val example = it.getString(idxExample) ?: ""
                        val subject = it.getString(idxSubject) ?: ""

                        val reason = when {
                            term.isBlank() -> "term is empty/blank"
                            kannadaTranslation.isBlank() -> "kannada_translation is empty/blank"
                            kannadaExplanation.isBlank() -> "kannada_explanation is empty/blank"
                            example.isBlank() -> "example is empty/blank"
                            subject !in validSubjects -> "subject '$subject' is not a valid Subject enum value"
                            else -> null
                        }

                        if (reason != null) {
                            Log.e(TAG, "Deleting malformed entry id=$id (term='$term'): $reason")
                            idsToDelete.add(id)
                        }
                    }
                }

                if (idsToDelete.isNotEmpty()) {
                    val placeholders = idsToDelete.joinToString(",") { "?" }
                    val args = idsToDelete.map { it.toString() }.toTypedArray()
                    db.execSQL("DELETE FROM entry WHERE id IN ($placeholders)", args)
                    Log.e(TAG, "Removed ${idsToDelete.size} malformed entries from database.")
                }
            }
        }
    }
}
