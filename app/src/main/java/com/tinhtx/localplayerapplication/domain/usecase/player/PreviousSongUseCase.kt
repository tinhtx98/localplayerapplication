package com.tinhtx.localplayerapplication.domain.usecase.player

import com.tinhtx.localplayerapplication.domain.model.Song
import javax.inject.Inject

class PreviousSongUseCase @Inject constructor() {
    operator fun invoke(currentQueue: List<Song>, currentIndex: Int, isShuffled: Boolean): Song? {
        if (currentQueue.isEmpty()) return null
        
        val previousIndex = if (isShuffled) {
            // Generate random index excluding current
            val availableIndices = currentQueue.indices.filter { it != currentIndex }
            if (availableIndices.isNotEmpty()) {
                availableIndices.random()
            } else {
                currentIndex
            }
        } else {
            if (currentIndex <= 0) currentQueue.size - 1 else currentIndex - 1
        }
        
        return currentQueue.getOrNull(previousIndex)
    }
}
