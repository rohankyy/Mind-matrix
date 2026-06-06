package com.nallanudi.app

import android.app.Application
import com.nallanudi.app.data.db.AppDatabase
import com.nallanudi.app.data.repository.GlossaryRepository
import com.nallanudi.app.data.repository.MyListRepository

/**
 * Application class — initialises singletons for database and repositories.
 * All dependencies are provided here and accessed by ViewModelFactory instances.
 * No network permissions are declared (Requirement 7.1).
 */
class NallaNudiApplication : Application() {

    // Lazy singletons — initialised on first access
    val database: AppDatabase by lazy {
        AppDatabase.build(this)
    }

    val glossaryRepository: GlossaryRepository by lazy {
        GlossaryRepository(database.entryDao(), database.wordOfDayHistoryDao())
    }

    val myListRepository: MyListRepository by lazy {
        MyListRepository(database.myListDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Trigger database initialisation on app start so the first query is fast
        // (pre-populated asset is copied on first access)
    }
}
