package com.singularitycoder.viewmodelstuff2.helpers.animedb

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.singularitycoder.viewmodelstuff2.helpers.constants.Table
import com.singularitycoder.viewmodelstuff2.helpers.extensions.trimIndentsAndNewLines

// https://stackoverflow.com/questions/52657760/android-room-how-to-migrate-column-renaming
// https://stackoverflow.com/questions/805363/how-do-i-rename-a-column-in-a-sqlite-database-table
// https://developer.android.com/reference/android/database/sqlite/package-summary

// What types of data can each cell of a table hold? - Boolean is Integer, String is Text, Float is Float

internal val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${Table.ANIME_DATA} ADD COLUMN myFavReason TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${Table.ANIME_DATA} ADD COLUMN myFavReasonDate TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${Table.ANIME_DATA} ADD COLUMN isFav INTEGER DEFAULT 0 NOT NULL")
    }
}

internal val MIGRATION_2_TO_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // Old way to alter column name
        /*migrateIsFavColumnNameOfTableAnimeDataInTheOldWay(database)*/

        // New way to alter column name - since SQLite version 3.25
        database.execSQL("ALTER TABLE ${Table.ANIME_DATA} RENAME COLUMN isFav TO isFavourite")
    }

    private fun migrateIsFavColumnNameOfTableAnimeDataInTheOldWay(database: SupportSQLiteDatabase) {
        val TABLE_ANIME_DATA_TEMPORARY = "table_anime_data_temporary"

        fun setTableAnimeDataColumnNames(columnNameToChange: String): String = """aniListId, format, status, startDate, endDate, seasonPeriod,
            seasonYear, episodesCount, episodeDuration, trailerUrl, coverImage, bannerImage, genres, score, id, myFavReason, myFavReasonDate,
            $columnNameToChange, title_en, title_jp, title_it, desc_descId, desc_en, desc_it""".trimIndentsAndNewLines()

        // If u r adding new tables, then you can directly copy the SQL queries from the generated java dir below the test folder  ->  com.singularitycoder.viewmodelstuff2 -> db -> FavAnimeDatabase_Impl
        // 1. Create the new table with the new column name - `isFavourite` INTEGER DEFAULT 0 NOT NULL
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `$TABLE_ANIME_DATA_TEMPORARY` (
                `aniListId` INTEGER NOT NULL,
                `format` INTEGER NOT NULL,
                `status` INTEGER NOT NULL,
                `startDate` TEXT NOT NULL,
                `endDate` TEXT NOT NULL,
                `seasonPeriod` INTEGER NOT NULL,
                `seasonYear` INTEGER NOT NULL,
                `episodesCount` INTEGER NOT NULL,
                `episodeDuration` INTEGER NOT NULL,
                `trailerUrl` TEXT NOT NULL,
                `coverImage` TEXT NOT NULL,
                `bannerImage` TEXT NOT NULL,
                `genres` TEXT NOT NULL,
                `score` INTEGER NOT NULL,
                `id` INTEGER NOT NULL,
                `myFavReason` TEXT NOT NULL DEFAULT '',
                `myFavReasonDate` TEXT NOT NULL DEFAULT '',
                `isFavourite` INTEGER DEFAULT 0 NOT NULL,
                `title_en` TEXT,
                `title_jp` TEXT,
                `title_it` TEXT,
                `desc_descId` INTEGER NOT NULL,
                `desc_en` TEXT NOT NULL DEFAULT '',
                `desc_it` TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(`aniListId`))
            """.trimIndentsAndNewLines()
        )

        // 2. Copy the data from the old table to new table
        database.execSQL(
            """
                INSERT INTO $TABLE_ANIME_DATA_TEMPORARY (${setTableAnimeDataColumnNames(columnNameToChange = "isFavourite")})
                SELECT ${setTableAnimeDataColumnNames(columnNameToChange = "isFav")}
                FROM ${Table.ANIME_DATA}
            """.trimIndentsAndNewLines()
        )

        // 3. Remove the old table.
        database.execSQL("DROP TABLE ${Table.ANIME_DATA}")

        // 4. Change the table name to the correct one. Do this at the end as it seems the data might get corrupted if done in the beginning. Do it in this order
        database.execSQL("ALTER TABLE table_anime_data_temporary RENAME TO ${Table.ANIME_DATA}")
    }
}

internal val MIGRATION_3_TO_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${Table.ANIME_DATA} ADD COLUMN weeklyAiringDay INTEGER DEFAULT 0 NOT NULL")
    }
}
