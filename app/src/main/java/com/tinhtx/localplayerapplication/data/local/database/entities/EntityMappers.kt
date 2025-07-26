package com.tinhtx.localplayerapplication.data.local.database.entities

import com.tinhtx.localplayerapplication.domain.model.*

// Song Entity to Domain
fun SongEntity.toDomain(): Song = Song(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    data = data,
    dateAdded = dateAdded,
    albumId = albumId,
    artistId = artistId,
    track = track,
    year = year,
    size = size,
    mimeType = mimeType,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayed = lastPlayed
)

// Album Entity to Domain
fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    mediaStoreId = mediaStoreId,
    name = albumName,
    artist = artist,
    artistId = artistId,
    songCount = songCount,
    firstYear = firstYear,
    lastYear = lastYear,
    albumArtPath = albumArtPath
)

// Artist Entity to Domain
fun ArtistEntity.toDomain(): Artist = Artist(
    id = id,
    mediaStoreId = mediaStoreId,
    name = artistName,
    albumCount = albumCount,
    trackCount = trackCount,
    artistArtPath = artistArtPath
)

// Playlist Entity to Domain
fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    coverArtPath = coverArtPath,
    songCount = songCount,
    duration = duration,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Domain to Entity
fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    description = description,
    coverArtPath = coverArtPath,
    songCount = songCount,
    duration = duration,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt
)
