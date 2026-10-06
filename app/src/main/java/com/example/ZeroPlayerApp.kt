package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.MusicRepository

class ZeroPlayerApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: MusicRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = MusicRepository(database, this)
    }
}
