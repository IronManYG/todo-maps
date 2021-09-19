package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    // Use a fake DataSource to be injected into the viewModel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setUpSaveReminderViewModel() {
        val reminderDataItem = mutableListOf<ReminderDTO>()
        dataSource = FakeDataSource(reminderDataItem)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

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
    fun onClear_clearReminderLiveDataObjects(){
        // Given a data source containing emptyList
        dataSource = FakeDataSource()

        // Given a fresh SaveReminderViewModel
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When clear live data objects
        saveReminderViewModel.onClear()

        // Then live date objects is null
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), nullValue())
    }

    @Test
    fun saveReminder_saveReminderToDataSource(){
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // Given a data source containing emptyList
        dataSource = FakeDataSource()

        // Given  a reminder
        val reminder = ReminderDataItem("Title", "Description","Location", 1.0,1.0)

        // Given a fresh SaveReminderViewModel
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // When save reminder
        saveReminderViewModel.saveReminder(reminder)

        // Then assert that the progress indicator is shown.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is not shown.
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

        // Then assert that showToast value is Reminder Saved !
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

    }

    @Test
    fun validateEnteredData_returnFalseForReminderWithoutTitle(){
        // Given a reminder without title
        val reminder = ReminderDataItem("", "Description1","Location1", 1.0,1.0)

        // When validate entered data
        val value = saveReminderViewModel.validateEnteredData(reminder)

        // Then assert that return value validate entered data is false
        assertThat(value).isFalse()

        // Then assert that showSnackBarInt value is Please enter title
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)

    }

    @Test
    fun validateEnteredData_returnFalseForReminderWithoutLocation(){
        // Given a reminder without location
        val reminder = ReminderDataItem("Title", "Description","", 1.0,1.0)

        // When validate entered data
        val value = saveReminderViewModel.validateEnteredData(reminder)

        // Then assert that return value validate entered data is false
        assertThat(value).isFalse()

        // Then assert that showSnackBarInt value is Please select location
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

}