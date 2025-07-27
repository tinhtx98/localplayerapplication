package com.tinhtx.localplayerapplication.data.local.database.entities

import com.tinhtx.localplayerapplication.domain.model.*

/**
 * Extension functions to convert between entities and domain models
 */

// SongEntity Mappers
fun SongEntity.toDomain(): Song = Song(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    path = path,
    size = size,
    mimeType = mimeType,
    dateAdded = dateAdded,
    dateModified = dateModified,
    year = year,
    trackNumber = trackNumber,
    genre = genre,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayed = lastPlayed
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    path = path,
    size = size,
    mimeType = mimeType,
    dateAdded = dateAdded,
    dateModified = dateModified,
    year = year,
    trackNumber = trackNumber,
    genre = genre,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayed = lastPlayed
)

// AlbumEntity Mappers
fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    mediaStoreId = mediaStoreId,
    name = name,
    artist = artist,
    artistId = artistId,
    year = year,
    songCount = songCount,
    artworkPath = artworkPath
)

fun Album.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    mediaStoreId = mediaStoreId,
    name = name,
    artist = artist,
    artistId = artistId,
    year = year,
    songCount = songCount,
    artworkPath = artworkPath
)

// ArtistEntity Mappers
fun ArtistEntity.toDomain(): Artist = Artist(
    id = id,
    name = name,
    albumCount = albumCount,
    songCount = songCount,
    artworkPath = artworkPath
)

fun Artist.toEntity(): ArtistEntity = ArtistEntity(
    id = id,
    name = name,
    albumCount = albumCount,
    songCount = songCount,
    artworkPath = artworkPath
)

// PlaylistEntity Mappers
fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    songCount = songCount,
    duration = duration,
    artworkPath = artworkPath
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    songCount = songCount,
    duration = duration,
    artworkPath = artworkPath
)

// CastDeviceEntity Mappers
fun CastDeviceEntity.toDomain(): CastDevice = CastDevice(
    id = id,
    name = name,
    type = type,
    isAvailable = isAvailable,
    isConnected = isConnected,
    lastConnected = lastConnected,
    capabilities = capabilities
)

fun CastDevice.toEntity(): CastDeviceEntity = CastDeviceEntity(
    id = id,
    name = name,
    type = type,
    isAvailable = isAvailable,
    isConnected = isConnected,
    lastConnected = lastConnected,
    capabilities = capabilities,
    updatedAt = System.currentTimeMillis()
)


// ❌ REMOVED: FavoriteEntity, HistoryEntity, PlaylistSongCrossRef mappers
// These will be handled differently to match tree structure

// Batch conversion functions
fun List<SongEntity>.toDomainList(): List<Song> = map { it.toDomain() }
fun List<Song>.toEntityList(): List<SongEntity> = map { it.toEntity() }

fun List<AlbumEntity>.toDomainAlbums(): List<Album> = map { it.toDomain() }
fun List<Album>.toEntityAlbums(): List<AlbumEntity> = map { it.toEntity() }

fun List<ArtistEntity>.toDomainArtists(): List<Artist> = map { it.toDomain() }
fun List<Artist>.toEntityArtists(): List<ArtistEntity> = map { it.toEntity() }

fun List<PlaylistEntity>.toDomainPlaylists(): List<Playlist> = map { it.toDomain() }
fun List<Playlist>.toEntityPlaylists(): List<PlaylistEntity> = map { it.toEntity() }

fun List<CastDeviceEntity>.toDomainCastDevices(): List<CastDevice> = map { it.toDomain() }
fun List<CastDevice>.toEntityCastDevices(): List<CastDeviceEntity> = map { it.toEntity() }