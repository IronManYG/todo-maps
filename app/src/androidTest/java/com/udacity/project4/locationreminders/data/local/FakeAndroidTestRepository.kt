package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap

class FakeAndroidTestRepository(
    private val remindersDao: RemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ReminderDataSource {

    var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    suspend fun setReturnError(value: Boolean) = withContext(ioDispatcher)  {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(ioDispatcher) {
        if (shouldReturnError) {
            return@withContext Result.Error("Test exception")
        }

        return@withContext Result.Success(remindersServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        withContext(ioDispatcher) {
            remindersServiceData[reminder.id] = reminder
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> = withContext(ioDispatcher){
        if (shouldReturnError) {
            return@withContext  Result.Error("Test exception")
        }
        remindersServiceData[id]?.let {
            return@withContext Result.Success(it)
        }

        return@withContext Result.Error("Could not find task")
    }

    override suspend fun deleteAllReminders() {
        withContext(ioDispatcher) {
            remindersServiceData.clear()
        }
    }
}