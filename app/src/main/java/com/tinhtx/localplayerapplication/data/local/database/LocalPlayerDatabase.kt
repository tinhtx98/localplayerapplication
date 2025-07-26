package com.tinhtx.localplayerapplication.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tinhtx.localplayerapplication.data.local.database.converters.Converters
import com.tinhtx.localplayerapplication.data.local.database.dao.*
import com.tinhtx.localplayerapplication.data.local.database.entities.*

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        HistoryEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LocalPlayerDatabase : RoomDatabase() {
    
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
}
