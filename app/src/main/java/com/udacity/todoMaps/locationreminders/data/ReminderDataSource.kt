package com.udacity.todoMaps.locationreminders.data

import com.udacity.todoMaps.locationreminders.data.dto.ReminderDTO
import com.udacity.todoMaps.locationreminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders()
}