package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var datasource: ReminderDataSource

    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun startKoinForTest() {
        stopKoin()// stop the original app koin, which is launched when the application starts (in "MyApp")
        appContext = getApplicationContext()
        val myModule = module {
            // define your module for test here
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            // RemindersLocalRepository
            single<RemindersLocalRepository> { RemindersLocalRepository(get()) }
            // ReminderDataSource
            single<ReminderDataSource> { get<RemindersLocalRepository>() }
            // FakeAndroidTestRepository
            single<FakeAndroidTestRepository> { FakeAndroidTestRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        // Get our repository
        datasource = GlobalContext.get().koin.get()

        runBlocking {
            datasource.deleteAllReminders()
        }
    }

    @After
    fun stopKoinAfterTest() = stopKoin()

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //    : test the navigation of the fragments.

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario) // LOOK HERE

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the add screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder())
    }

    //    : test the displayed data on the UI.

    @Test
    fun reminderListFragment_DisplayedInUiWithReminders() {
        // Given reminders in the db
        val reminder1 = ReminderDTO("title1", "description1", "location1",
            1.0, 1.0)

        val reminder2 = ReminderDTO("title2", "description2", "location2",
            2.0, 2.0)

        runBlocking {
            datasource.saveReminder(reminder1)
            datasource.saveReminder(reminder2)
        }

        // When fragment is launched
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario) // LOOK HERE

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - Verify that
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.description)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))

        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder2.location)).check(matches(isDisplayed()))

    }

    @Test
    fun reminderListFragment_DisplayedInUiWithNoReminders() {
        // When fragment is launched
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario) // LOOK HERE

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Then - Verify that
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
    }

    //    TODO: add testing for the error messages.
//    @Test
//    fun reminderListFragment_ShowSnackBarWithErrorMessage() {
//        var message:String? = null
//
//        // When loaded reminders from data source get error
//        runBlocking {
//            datasource.setReturnError(true)
//            val result = datasource.getReminders() as Result.Error
//            message = result.message
//        }
//        // When fragment is launched
//        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//        dataBindingIdlingResource.monitorFragment(scenario) // LOOK HERE
//
//        val navController = mock(NavController::class.java)
//        scenario.onFragment {
//            Navigation.setViewNavController(it.view!!, navController)
//        }
//
//        // Then - Verify that
//        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
//        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(message)))
//    }

}