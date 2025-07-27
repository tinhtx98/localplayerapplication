package com.tinhtx.localplayerapplication.domain.usecase.user

import com.tinhtx.localplayerapplication.domain.model.UserProfile
import com.tinhtx.localplayerapplication.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Use case for updating user profile
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Update complete user profile
     */
    suspend fun execute(userProfile: UserProfile): Result<Unit> {
        return try {
            val validationResult = validateProfile(userProfile)
            if (!validationResult.isValid) {
                return Result.failure(Exception(validationResult.message))
            }
            
            userPreferencesRepository.updateUserProfile(userProfile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user name
     */
    suspend fun updateUserName(name: String): Result<Unit> {
        return try {
            val validationResult = validateName(name)
            if (!validationResult.isValid) {
                return Result.failure(Exception(validationResult.message))
            }
            
            userPreferencesRepository.updateUserName(name.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user email
     */
    suspend fun updateUserEmail(email: String): Result<Unit> {
        return try {
            val validationResult = validateEmail(email)
            if (!validationResult.isValid) {
                return Result.failure(Exception(validationResult.message))
            }
            
            userPreferencesRepository.updateUserEmail(email.trim().lowercase())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user avatar
     */
    suspend fun updateUserAvatar(avatarPath: String?): Result<Unit> {
        return try {
            if (avatarPath != null) {
                val validationResult = validateAvatarPath(avatarPath)
                if (!validationResult.isValid) {
                    return Result.failure(Exception(validationResult.message))
                }
            }
            
            userPreferencesRepository.updateUserAvatar(avatarPath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update last login timestamp
     */
    suspend fun updateLastLogin(): Result<Unit> {
        return try {
            userPreferencesRepository.updateLastLogin()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear user profile
     */
    suspend fun clearUserProfile(): Result<Unit> {
        return try {
            userPreferencesRepository.clearUserProfile()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user statistics
     */
    suspend fun updateUserStatistics(statistics: UserStatisticsUpdate): Result<Unit> {
        return try {
            statistics.totalListeningTime?.let { time ->
                userPreferencesRepository.updateTotalListeningTime(time)
            }
            
            statistics.songsPlayedCount?.let { count ->
                userPreferencesRepository.updateSongsPlayedCount(count)
            }
            
            statistics.favoriteGenre?.let { genre ->
                userPreferencesRepository.updateFavoriteGenre(genre)
            }
            
            statistics.favoriteArtist?.let { artist ->
                userPreferencesRepository.updateFavoriteArtist(artist)
            }
            
            statistics.longestSession?.let { duration ->
                userPreferencesRepository.updateLongestSession(duration)
            }
            
            statistics.averageSessionLength?.let { length ->
                userPreferencesRepository.updateAverageSessionLength(length)
            }
            
            statistics.mostActiveHour?.let { hour ->
                userPreferencesRepository.updateMostActiveHour(hour)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update listening goals
     */
    suspend fun updateListeningGoals(
        weeklyGoalMinutes: Long?,
        monthlyGoalMinutes: Long?
    ): Result<Unit> {
        return try {
            weeklyGoalMinutes?.let { goal ->
                if (goal < 0 || goal > 10080) { // 0 to 7 days in minutes
                    return Result.failure(Exception("Weekly goal must be between 0 and 10080 minutes"))
                }
                userPreferencesRepository.setWeeklyListeningGoal(goal)
            }
            
            monthlyGoalMinutes?.let { goal ->
                if (goal < 0 || goal > 43800) { // 0 to 30.4 days in minutes
                    return Result.failure(Exception("Monthly goal must be between 0 and 43800 minutes"))
                }
                userPreferencesRepository.setMonthlyListeningGoal(goal)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update listening progress
     */
    suspend fun updateListeningProgress(
        weeklyProgress: Long?,
        monthlyProgress: Long?
    ): Result<Unit> {
        return try {
            weeklyProgress?.let { progress ->
                userPreferencesRepository.updateWeeklyProgress(progress)
            }
            
            monthlyProgress?.let { progress ->
                userPreferencesRepository.updateMonthlyProgress(progress)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save session data
     */
    suspend fun saveSessionData(songId: Long, position: Long): Result<Unit> {
        return try {
            userPreferencesRepository.saveLastPlayedSong(songId, position)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save queue data
     */
    suspend fun saveQueueData(songIds: List<Long>, position: Int): Result<Unit> {
        return try {
            userPreferencesRepository.saveLastQueue(songIds, position)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Increment app launch count
     */
    suspend fun incrementAppLaunchCount(): Result<Unit> {
        return try {
            userPreferencesRepository.incrementAppLaunchCount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create backup of user data
     */
    suspend fun createBackup(): Result<String> {
        return try {
            val backupData = userPreferencesRepository.createBackup()
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restore user data from backup
     */
    suspend fun restoreFromBackup(backupData: String): Result<Unit> {
        return try {
            if (backupData.isBlank()) {
                return Result.failure(Exception("Backup data cannot be empty"))
            }
            
            val success = userPreferencesRepository.restoreFromBackup(backupData)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to restore from backup"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear all user preferences
     */
    suspend fun clearAllPreferences(): Result<Unit> {
        return try {
            userPreferencesRepository.clearAllPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Validation methods
    private fun validateProfile(profile: UserProfile): ValidationResult {
        val nameValidation = validateName(profile.name)
        if (!nameValidation.isValid) return nameValidation
        
        val emailValidation = validateEmail(profile.email)
        if (!emailValidation.isValid) return emailValidation
        
        if (profile.avatarPath != null) {
            val avatarValidation = validateAvatarPath(profile.avatarPath)
            if (!avatarValidation.isValid) return avatarValidation
        }
        
        return ValidationResult(true, "Profile is valid")
    }
    
    private fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name cannot be empty")
            name.length < 2 -> ValidationResult(false, "Name must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "Name cannot exceed 50 characters")
            !name.matches(Regex("^[a-zA-Z0-9\\s._-]+$")) -> ValidationResult(false, "Name contains invalid characters")
            else -> ValidationResult(true, "Valid name")
        }
    }
    
    private fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email cannot be empty")
            email.length > 100 -> ValidationResult(false, "Email cannot exceed 100 characters")
            !isValidEmail(email) -> ValidationResult(false, "Invalid email format")
            else -> ValidationResult(true, "Valid email")
        }
    }
    
    private fun validateAvatarPath(avatarPath: String): ValidationResult {
        return when {
            avatarPath.isBlank() -> ValidationResult(false, "Avatar path cannot be empty")
            avatarPath.length > 500 -> ValidationResult(false, "Avatar path too long")
            !isValidFilePath(avatarPath) -> ValidationResult(false, "Invalid avatar path")
            else -> ValidationResult(true, "Valid avatar path")
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }
    
    private fun isValidFilePath(path: String): Boolean {
        // Basic file path validation
        return try {
            java.io.File(path)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class for user statistics updates
 */
data class UserStatisticsUpdate(
    val totalListeningTime: Long? = null,
    val songsPlayedCount: Int? = null,
    val favoriteGenre: String? = null,
    val favoriteArtist: String? = null,
    val longestSession: Long? = null,
    val averageSessionLength: Long? = null,
    val mostActiveHour: Int? = null
)

/**
 * Data class for validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)
