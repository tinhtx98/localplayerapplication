package com.tinhtx.localplayerapplication.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.data.local.database.converters.Converters
import com.tinhtx.localplayerapplication.data.local.database.dao.*
import com.tinhtx.localplayerapplication.data.local.database.entities.*

/**
 * Room database for LocalPlayer application
 */
@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        CastDeviceEntity::class 
    ],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LocalPlayerDatabase : RoomDatabase() {

    // DAO Abstract Methods
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun castDeviceDao(): CastDeviceDao

    companion object {
        
        /**
         * Create database instance with proper configuration
         */
        fun create(
            context: android.content.Context,
            useInMemory: Boolean = false
        ): LocalPlayerDatabase {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, LocalPlayerDatabase::class.java)
            } else {
                Room.databaseBuilder(
                    context,
                    LocalPlayerDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
            }

            return databaseBuilder
                .addTypeConverter(Converters())
                .addCallback(DatabaseCallback())
                .addMigrations(*getAllMigrations())
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Get all database migrations
         */
        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // Add future migrations here
                // MIGRATION_1_2,
                // MIGRATION_2_3,
                // etc.
            )
        }

        /**
         * Database callback for initialization
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database created for the first time
                createIndexes(db)
                insertInitialData(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Database opened
                enableForeignKeys(db)
                optimizeDatabase(db)
            }

            /**
             * Create additional indexes for performance
             */
            private fun createIndexes(db: SupportSQLiteDatabase) {
                // Songs indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_title ON songs(title)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_artist ON songs(artist)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_album ON songs(album)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_genre ON songs(genre)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_year ON songs(year)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_path ON songs(path)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_mediaStoreId ON songs(mediaStoreId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_dateAdded ON songs(dateAdded)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_playCount ON songs(playCount)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_isFavorite ON songs(isFavorite)")

                // Albums indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_albums_name ON albums(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_albums_artist ON albums(artist)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_albums_year ON albums(year)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_albums_mediaStoreId ON albums(mediaStoreId)")

                // Artists indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_artists_name ON artists(name)")

                // Playlists indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlists_name ON playlists(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlists_createdAt ON playlists(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playlists_updatedAt ON playlists(updatedAt)")

                // History indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_playedAt ON history(playedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_sessionId ON history(sessionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_source ON history(source)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_completionPercentage ON history(completionPercentage)")

                // Cast devices indexes 🆕
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_name ON cast_devices(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_type ON cast_devices(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_isAvailable ON cast_devices(isAvailable)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_isConnected ON cast_devices(isConnected)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_lastConnected ON cast_devices(lastConnected)")

                // Composite indexes for common queries
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_artist_album ON songs(artist, album)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_album_trackNumber ON songs(album, trackNumber)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_history_songId_playedAt ON history(songId, playedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cast_devices_available_connected ON cast_devices(isAvailable, isConnected)")
            }

            /**
             * Insert initial data if needed
             */
            private fun insertInitialData(db: SupportSQLiteDatabase) {
                // Insert default playlists or other initial data
                val currentTime = System.currentTimeMillis()
                
                // Create default "Favorites" playlist (optional)
                // db.execSQL("""
                //     INSERT OR IGNORE INTO playlists (name, description, createdAt, updatedAt)
                //     VALUES ('Favorites', 'Your favorite songs', $currentTime, $currentTime)
                // """)
            }

            /**
             * Enable foreign key constraints
             */
            private fun enableForeignKeys(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys = ON")
            }

            /**
             * Optimize database settings
             */
            private fun optimizeDatabase(db: SupportSQLiteDatabase) {
                // Enable WAL mode for better concurrent access
                db.execSQL("PRAGMA journal_mode = WAL")
                
                // Set synchronous mode for better performance
                db.execSQL("PRAGMA synchronous = NORMAL")
                
                // Set cache size (in KB)
                db.execSQL("PRAGMA cache_size = -2000") // 2MB cache
                
                // Enable memory-mapped I/O
                db.execSQL("PRAGMA mmap_size = 134217728") // 128MB
                
                // Optimize for space
                db.execSQL("PRAGMA auto_vacuum = INCREMENTAL")
            }
        }

        // Example migration (for future use)
        /*
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add a new column
                database.execSQL("ALTER TABLE songs ADD COLUMN lyrics TEXT")
                
                // Example: Create new table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS lyrics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        songId INTEGER NOT NULL,
                        lyrics TEXT,
                        source TEXT,
                        FOREIGN KEY(songId) REFERENCES songs(id) ON DELETE CASCADE
                    )
                """)
                
                // Example: Create index
                database.execSQL("CREATE INDEX IF NOT EXISTS index_lyrics_songId ON lyrics(songId)")
            }
        }
        */
    }

    /**
     * Clear all data from database
     */
    suspend fun clearAllData() {
        clearAllTables()
    }

    /**
     * Get database size in bytes
     */
    fun getDatabaseSize(): Long {
        return try {
            val dbFile = java.io.File(this.openHelper.readableDatabase.path ?: "")
            if (dbFile.exists()) dbFile.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Vacuum database to reclaim space
     */
    suspend fun vacuumDatabase() {
        openHelper.writableDatabase.execSQL("VACUUM")
    }

    /**
     * Analyze database for query optimization
     */
    suspend fun analyzeDatabase() {
        openHelper.writableDatabase.execSQL("ANALYZE")
    }

    /**
     * Get database info
     */
    fun getDatabaseInfo(): DatabaseInfo {
        val db = openHelper.readableDatabase
        return DatabaseInfo(
            version = db.version,
            path = db.path ?: "",
            pageSize = db.pageSize,
            maxSize = db.maximumSize,
            size = getDatabaseSize()
        )
    }

    /**
     * Check database integrity
     */
    suspend fun checkIntegrity(): Boolean {
        return try {
            val cursor = openHelper.readableDatabase.rawQuery("PRAGMA integrity_check", null)
            cursor.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    result == "ok"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get table information - 🆕 Updated to include cast devices
     */
    suspend fun getTableInfo(): List<TableInfo> {
        val tables = mutableListOf<TableInfo>()
        val db = openHelper.readableDatabase
        
        val tableNames = listOf(
            "songs", "albums", "artists", "playlists", 
            "playlist_song_cross_ref", "favorites", "history", 
            "cast_devices" // 🆕 Added cast devices table
        )
        
        for (tableName in tableNames) {
            try {
                val cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
                cursor.use {
                    if (it.moveToFirst()) {
                        val count = it.getInt(0)
                        tables.add(TableInfo(tableName, count))
                    }
                }
            } catch (e: Exception) {
                tables.add(TableInfo(tableName, 0))
            }
        }
        
        return tables
    }

    /**
     * Export database to file (for backup)
     */
    suspend fun exportToFile(targetPath: String): Boolean {
        return try {
            val currentDbPath = openHelper.readableDatabase.path
            if (currentDbPath != null) {
                val currentFile = java.io.File(currentDbPath)
                val targetFile = java.io.File(targetPath)
                currentFile.copyTo(targetFile, overwrite = true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Database information data class
 */
data class DatabaseInfo(
    val version: Int,
    val path: String,
    val pageSize: Long,
    val maxSize: Long,
    val size: Long
)

/**
 * Table information data class
 */
data class TableInfo(
    val name: String,
    val rowCount: Int
)
