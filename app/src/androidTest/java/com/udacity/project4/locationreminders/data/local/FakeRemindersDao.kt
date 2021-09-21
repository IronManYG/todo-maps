package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class FakeRemindersDao(var reminders: MutableList<ReminderDTO>? = mutableListOf()): RemindersDao {

    override suspend fun getReminders(): List<ReminderDTO> {
        reminders?.let {
            return ArrayList(it) }
        return reminders!!
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        reminders?.forEach {
            if(it.id == reminderId){
                 return it
            }
        }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}