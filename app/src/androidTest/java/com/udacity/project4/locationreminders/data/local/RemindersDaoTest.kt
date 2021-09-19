package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //: Add testing implementation to the RemindersDao.kt

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun getReminders_andCheckThemValueByID() = runBlockingTest {
        // Given reminders
        val reminder1 = ReminderDTO("Title1", "Description1","Location1", 1.0,1.0)

        val reminder2 = ReminderDTO("Title2", "Description2","Location2", 2.0,2.0)

        database.reminderDao().saveReminder(reminder1)

        database.reminderDao().saveReminder(reminder2)

        // When the reminders are gets
        val remindersList = database.reminderDao().getReminders()

        // Then The loaded data contains the expected values.
        assertThat(remindersList[0].id, `is`(reminder1.id))
        assertThat(remindersList[0].title, `is`(reminder1.title))
        assertThat(remindersList[0].description, `is`(reminder1.description))
        assertThat(remindersList[0].location, `is`(reminder1.location))
        assertThat(remindersList[0].latitude, `is`(reminder1.latitude))
        assertThat(remindersList[0].longitude, `is`(reminder1.longitude))

        assertThat(remindersList[1].id, `is`(reminder2.id))
        assertThat(remindersList[1].title, `is`(reminder2.title))
        assertThat(remindersList[1].description, `is`(reminder2.description))
        assertThat(remindersList[1].location, `is`(reminder2.location))
        assertThat(remindersList[1].latitude, `is`(reminder2.latitude))
        assertThat(remindersList[1].longitude, `is`(reminder2.longitude))
    }

    @Test
    fun getReminderById_andCheckITValueByID() = runBlockingTest {
        // Given a reminder
        val reminder = ReminderDTO("Title1", "Description1","Location1", 1.0,1.0)

        database.reminderDao().saveReminder(reminder)

        // When the reminder are gets
        val loadedReinder = database.reminderDao().getReminderById(reminder.id)

        // Then The loaded data contains the expected values.
        assertThat(loadedReinder!!.id, `is`(reminder.id))
        assertThat(loadedReinder.title, `is`(reminder.title))
        assertThat(loadedReinder.description, `is`(reminder.description))
        assertThat(loadedReinder.location, `is`(reminder.location))
        assertThat(loadedReinder.latitude, `is`(reminder.latitude))
        assertThat(loadedReinder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders_andGetEmptyList() = runBlockingTest {
        // Given reminders
        val reminder1 = ReminderDTO("Title1", "Description1","Location1", 1.0,1.0)

        val reminder2 = ReminderDTO("Title2", "Description2","Location2", 2.0,2.0)

        database.reminderDao().saveReminder(reminder1)

        database.reminderDao().saveReminder(reminder2)

        // When the reminders are deleted
        database.reminderDao().deleteAllReminders()

        val remindersList = database.reminderDao().getReminders()
        // Then The loaded data contains the expected values
        assertThat(remindersList, `is`(emptyList()))
    }

}