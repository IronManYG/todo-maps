package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //: provide testing to the RemindersListViewModel and its live data objects

    // Use a fake DataSource to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    private val reminder1 = ReminderDTO("Title1", "Description1","Location1", 1.0,1.0)

    private val reminder2 = ReminderDTO("Title2", "Description2","Location2", 2.0,2.0)

    private val reminder3 = ReminderDTO("Title1", "Description1","Location1", 3.0,3.0)

    @Before
    fun setupFirebase() {
        // initialize the firebase
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun loadReminders_getsRemindersListForPopulatedRemindersList(){
        // Given  a data source containing a reminders list
        val localReminders = listOf(reminder1,reminder2,reminder3).sortedBy { it.id }

        dataSource = FakeDataSource(localReminders.toMutableList())

        // Given a fresh RemindersListViewModel
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When load reminders from data source
        remindersListViewModel.loadReminders()

        // Then remindersList is not empty
        val value = remindersListViewModel.remindersList.getOrAwaitValue()

        assertThat(value, (not(emptyList())))
    }

    @Test
    fun loadReminders_returnsErrorForNullRemindersList() {
        // Given a data source containing null
        dataSource=FakeDataSource(null)

        // Given a fresh RemindersListViewModel
        remindersListViewModel=
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        // When loading reminders
        remindersListViewModel.loadReminders()

        // Then a snack bar with the message "No reminders found" is shown
        val value = remindersListViewModel.showSnackBar.getOrAwaitValue()

        assertThat(value,`is`("Reminders not found!"))
    }

    @Test
    fun loadReminders_showLoading() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Given  a data source containing a reminders list
        val localReminders = listOf(reminder1,reminder2,reminder3).sortedBy { it.id }

        dataSource = FakeDataSource(localReminders.toMutableList())

        // Given a fresh RemindersListViewModel
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When load reminders from data source
        remindersListViewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_showNoData_for_nullList() {
        // Given  a data source containing a reminders list
        val localReminders = null

        dataSource = FakeDataSource(localReminders)

        // Given a fresh RemindersListViewModel
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When load reminders from data source
        remindersListViewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_showNoData_for_emptyList() {
        // Given a data source containing null
        dataSource = FakeDataSource()

        // Given a fresh RemindersListViewModel
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When load reminders from data source
        remindersListViewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
}