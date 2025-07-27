package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.domain.model.UserProfile
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for getting user profile information
 */
class GetUserProfileUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Get user profile
     */
    fun getUserProfile(): Flow<UserProfile> {
        return userPreferencesRepository.getUserProfile()
    }
    
    /**
     * Get current user profile snapshot
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user name
     */
    suspend fun getUserName(): Result<String> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user email
     */
    suspend fun getUserEmail(): Result<String> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile.email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user avatar path
     */
    suspend fun getUserAvatarPath(): Result<String?> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile.avatarPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user creation date
     */
    suspend fun getUserCreationDate(): Result<Long> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile.createdAt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get last login time
     */
    suspend fun getLastLoginTime(): Result<Long> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(profile.lastLogin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user profile is complete
     */
    suspend fun isProfileComplete(): Result<Boolean> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            val isComplete = profile.name.isNotBlank() && profile.email.isNotBlank()
            Result.success(isComplete)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user statistics
     */
    suspend fun getUserStatistics(): Result<UserStatistics> {
        return try {
            val totalListeningTime = userPreferencesRepository.getTotalListeningTime().first()
            val songsPlayedCount = userPreferencesRepository.getSongsPlayedCount().first()
            val favoriteGenre = userPreferencesRepository.getFavoriteGenre().first()
            val favoriteArtist = userPreferencesRepository.getFavoriteArtist().first()
            val longestSession = userPreferencesRepository.getLongestSession().first()
            val averageSessionLength = userPreferencesRepository.getAverageSessionLength().first()
            val mostActiveHour = userPreferencesRepository.getMostActiveHour().first()
            val appLaunchCount = userPreferencesRepository.getAppLaunchCount().first()
            
            val statistics = UserStatistics(
                totalListeningTimeHours = totalListeningTime / (1000.0 * 60.0 * 60.0),
                songsPlayedCount = songsPlayedCount.toInt(),
                favoriteGenre = favoriteGenre.ifBlank { "Unknown" },
                favoriteArtist = favoriteArtist.ifBlank { "Unknown" },
                longestSessionHours = longestSession / (1000.0 * 60.0 * 60.0),
                averageSessionMinutes = averageSessionLength / (1000.0 * 60.0),
                mostActiveHour = mostActiveHour,
                appLaunchCount = appLaunchCount
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get listening goals progress
     */
    suspend fun getListeningGoalsProgress(): Result<ListeningGoalsProgress> {
        return try {
            val weeklyGoal = userPreferencesRepository.getWeeklyListeningGoal().first()
            val monthlyGoal = userPreferencesRepository.getMonthlyListeningGoal().first()
            val weeklyProgress = userPreferencesRepository.getWeeklyProgress().first()
            val monthlyProgress = userPreferencesRepository.getMonthlyProgress().first()
            
            val goalsProgress = ListeningGoalsProgress(
                weeklyGoalMinutes = weeklyGoal,
                monthlyGoalMinutes = monthlyGoal,
                weeklyProgressMinutes = weeklyProgress,
                monthlyProgressMinutes = monthlyProgress,
                weeklyProgressPercentage = if (weeklyGoal > 0) {
                    (weeklyProgress.toFloat() / weeklyGoal) * 100f
                } else 0f,
                monthlyProgressPercentage = if (monthlyGoal > 0) {
                    (monthlyProgress.toFloat() / monthlyGoal) * 100f
                } else 0f
            )
            
            Result.success(goalsProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user preferences summary
     */
    suspend fun getUserPreferencesSummary(): Result<UserPreferencesSummary> {
        return try {
            val lastQueue = userPreferencesRepository.getLastQueue().first()
            val lastPlayedSong = userPreferencesRepository.getLastPlayedSong().first()
            
            val summary = UserPreferencesSummary(
                hasLastQueue = lastQueue.first.isNotEmpty(),
                lastQueueSize = lastQueue.first.size,
                hasLastPlayedSong = lastPlayedSong.first > 0,
                lastPlayedPosition = lastPlayedSong.second
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has avatar
     */
    suspend fun hasAvatar(): Result<Boolean> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            Result.success(!profile.avatarPath.isNullOrBlank())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile completion percentage
     */
    suspend fun getProfileCompletionPercentage(): Result<Float> {
        return try {
            val profile = userPreferencesRepository.getUserProfile().first()
            var completedFields = 0
            val totalFields = 3
            
            if (profile.name.isNotBlank()) completedFields++
            if (profile.email.isNotBlank()) completedFields++
            if (!profile.avatarPath.isNullOrBlank()) completedFields++
            
            val percentage = (completedFields.toFloat() / totalFields) * 100f
            Result.success(percentage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for user statistics
 */
data class UserStatistics(
    val totalListeningTimeHours: Double,
    val songsPlayedCount: Int,
    val favoriteGenre: String,
    val favoriteArtist: String,
    val longestSessionHours: Double,
    val averageSessionMinutes: Double,
    val mostActiveHour: Int,
    val appLaunchCount: Int
) {
    val formattedListeningTime: String
        get() = if (totalListeningTimeHours >= 1) {
            "${totalListeningTimeHours.toInt()}h ${((totalListeningTimeHours % 1) * 60).toInt()}m"
        } else {
            "${(totalListeningTimeHours * 60).toInt()}m"
        }
    
    val formattedMostActiveHour: String
        get() = String.format("%02d:00", mostActiveHour)
}

/**
 * Data class for listening goals progress
 */
data class ListeningGoalsProgress(
    val weeklyGoalMinutes: Long,
    val monthlyGoalMinutes: Long,
    val weeklyProgressMinutes: Long,
    val monthlyProgressMinutes: Long,
    val weeklyProgressPercentage: Float,
    val monthlyProgressPercentage: Float
) {
    val weeklyGoalMet: Boolean
        get() = weeklyProgressMinutes >= weeklyGoalMinutes
    
    val monthlyGoalMet: Boolean
        get() = monthlyProgressMinutes >= monthlyGoalMinutes
    
    val weeklyRemainingMinutes: Long
        get() = maxOf(0, weeklyGoalMinutes - weeklyProgressMinutes)
    
    val monthlyRemainingMinutes: Long
        get() = maxOf(0, monthlyGoalMinutes - monthlyProgressMinutes)
}

/**
 * Data class for user preferences summary
 */
data class UserPreferencesSummary(
    val hasLastQueue: Boolean,
    val lastQueueSize: Int,
    val hasLastPlayedSong: Boolean,
    val lastPlayedPosition: Long
) {
    val formattedLastPosition: String
        get() {
            val seconds = (lastPlayedPosition / 1000) % 60
            val minutes = (lastPlayedPosition / (1000 * 60)) % 60
            val hours = (lastPlayedPosition / (1000 * 60 * 60))
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}
