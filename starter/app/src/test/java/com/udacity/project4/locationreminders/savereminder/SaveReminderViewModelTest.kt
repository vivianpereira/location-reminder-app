package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    // subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // fake repo to be injected into viewmodel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRepository() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        fakeDataSource.shouldReturnError = false
        saveReminderViewModel = SaveReminderViewModel(
            getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun `onGeofenceCompleted - when reminder is saved then show toast, clear and navigate`() =
        runBlockingTest {
            // get a reminder
            val reminder = ReminderDataItem(
                "Title",
                "Description",
                "Location",
                26.19,
                32.25
            )

            saveReminderViewModel.onGeofenceCompleted(reminder)
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
            assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

            // clear
            assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(""))
            assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(""))
            assertThat(
                saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`("")
            )

            // navigation
            assertThat(
                saveReminderViewModel.navigationCommand.getOrAwaitValue(),
                `is`(NavigationCommand.Back)
            )
        }

    @Test
    fun `onGeofenceFailed - then show toast and hide loading`() =
        runBlockingTest {

        saveReminderViewModel.onGeofenceFailed()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Geofence service is not available now. Go to Settings>Location>Mode and choose High accuracy."))

    }

    @Test
    fun `onSaveReminder - check if the title is empty if yes show the snackBar Please enter title `() =
        runBlockingTest {

            saveReminderViewModel.reminderTitle.value = ""

            saveReminderViewModel.onSaveReminder()
            assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(""))
            assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        }

    @Test
    fun `onSaveReminder - show the loading then start the geofence`() =
        runBlockingTest {

            saveReminderViewModel.reminderTitle.value = "title"
            saveReminderViewModel.reminderSelectedLocationStr.value = "location"

            saveReminderViewModel.onSaveReminder()
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
            assertThat(saveReminderViewModel.startGeofence.getOrAwaitValue()?.title, `is`("title"))
            assertThat(saveReminderViewModel.startGeofence.getOrAwaitValue()?.location, `is`("location"))
            assertThat(saveReminderViewModel.startGeofence.getOrAwaitValue()?.latitude, `is`(Double.NEGATIVE_INFINITY))
            assertThat(saveReminderViewModel.startGeofence.getOrAwaitValue()?.longitude, `is`(Double.NEGATIVE_INFINITY))
        }

}