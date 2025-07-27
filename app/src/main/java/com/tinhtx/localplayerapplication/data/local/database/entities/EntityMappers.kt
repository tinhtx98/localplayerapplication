package com.tinhtx.localplayerapplication.data.local.database.entities

import com.tinhtx.localplayerapplication.domain.model.*

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

// HistoryEntity Mappers
fun HistoryEntity.toDomain(): PlayHistory = PlayHistory(
    id = id,
    songId = songId,
    playedAt = playedAt,
    playDuration = playDuration,
    completionPercentage = completionPercentage,
    source = source,
    sessionId = sessionId,
    skipped = skipped
)

fun PlayHistory.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    songId = songId,
    playedAt = playedAt,
    playDuration = playDuration,
    completionPercentage = completionPercentage,
    source = source,
    sessionId = sessionId,
    skipped = skipped
)

fun DetailedHistoryEntity.toDomain(): DetailedPlayHistory = DetailedPlayHistory(
    id = id,
    songId = songId,
    playedAt = playedAt,
    playDuration = playDuration,
    completionPercentage = completionPercentage,
    source = source,
    sessionId = sessionId,
    skipped = skipped,
    songTitle = title,
    songArtist = artist,
    songAlbum = album
)