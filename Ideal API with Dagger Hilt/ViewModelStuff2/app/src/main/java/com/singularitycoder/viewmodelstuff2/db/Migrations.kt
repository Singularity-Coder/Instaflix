package com.singularitycoder.viewmodelstuff2.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE table_anime_data ADD COLUMN myFavReason TEXT")
        database.execSQL("ALTER TABLE table_anime_data ADD COLUMN myFavReasonDate TEXT")
    }
}
