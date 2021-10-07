package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    : Add testing implementation to the RemindersLocalRepository.kt

    // Class under test
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var remindersDao: RemindersDao

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("Title1", "Description1","Location1", 1.0,1.0)

    private val reminder2 = ReminderDTO("Title2", "Description2","Location2", 2.0,2.0)

    private val reminder3 = ReminderDTO("Title3", "Description3","Location3", 3.0,3.0)

    private val localReminders = listOf(reminder1,reminder2,reminder3)

    @Before
    fun createRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        remindersDao = database.reminderDao()

        runBlocking {
            remindersDao.saveReminder(reminder1)
            remindersDao.saveReminder(reminder2)
            remindersDao.saveReminder(reminder3)
        }

        remindersLocalRepository = RemindersLocalRepository(remindersDao,Dispatchers.Main)
    }

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun getReminders_andCheckThemValue() = mainCoroutineRule.runBlockingTest {
        // When the reminders are gets from the reminders repository
        val remindersList = remindersLocalRepository.getReminders() as Result.Success

        // Then The loaded data contains the expected values.
        assertThat(remindersList.data, IsEqual(localReminders))
    }

    @Test
    fun getReminder_andCheckThemValueByID() = mainCoroutineRule.runBlockingTest {
        // When the reminder is gets from the reminders repository
        val reminder = remindersLocalRepository.getReminder(reminder1.id) as Result.Success

        // Then The loaded data contains the expected values.
        assertThat(reminder, `is`(notNullValue()))
        assertThat(reminder.data.title, `is`(reminder1.title))
        assertThat(reminder.data.location, `is`(reminder1.location))
        assertThat(reminder.data.latitude, `is`(reminder1.latitude))
        assertThat(reminder.data.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun getReminder_andCheckNullValueByID() = mainCoroutineRule.runBlockingTest {
        // When the reminder is gets from the reminders repository
        val reminder = remindersLocalRepository.getReminder("null") as Result.Error

        // Then The loaded data contains the expected values.
        assertThat(reminder.message, `is`("Reminder not found!"))
    }

    @Test
    fun saveReminder_andCheckThemValueByID() = mainCoroutineRule.runBlockingTest {
        // Given
        val reminder4 = ReminderDTO("Title4", "Description4","Location4", 4.0,4.0)

        remindersDao.saveReminder(reminder4)

        // When the reminder is gets from the reminders repository
        val reminder = remindersLocalRepository.getReminder(reminder4.id) as Result.Success

        // Then The loaded data contains the expected values.
        assertThat(reminder, `is`(notNullValue()))
        assertThat(reminder.data.title, `is`(reminder4.title))
        assertThat(reminder.data.location, `is`(reminder4.location))
        assertThat(reminder.data.latitude, `is`(reminder4.latitude))
        assertThat(reminder.data.longitude, `is`(reminder4.longitude))
    }

    @Test
    fun deleteAllReminders_andCheckEmptyValue() = mainCoroutineRule.runBlockingTest {
        // When the reminders are deleted
        remindersLocalRepository.deleteAllReminders()

        val remindersList = remindersLocalRepository.getReminders() as Result.Success

        // Then The loaded data contains the expected values.
        assertThat(remindersList, `is`(Result.Success(emptyList())))
    }

}