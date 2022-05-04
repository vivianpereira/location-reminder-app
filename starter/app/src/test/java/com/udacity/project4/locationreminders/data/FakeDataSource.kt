package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Reminders not found", 404)
        } else {
            return return Result.Success(ArrayList(reminders))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) {
            Result.Error("Reminder not found!")
        } else {
            try {
                val reminder = reminders?.find {
                    it.id == id
                }
                if (reminder != null) {
                    Result.Success(reminder)
                } else {
                    Result.Error("Reminder not found!")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage)
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}