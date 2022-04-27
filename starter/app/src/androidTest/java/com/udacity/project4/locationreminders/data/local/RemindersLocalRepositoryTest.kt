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
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initDatabase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries().build()
        remindersLocalRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun `get_reminder_with_id_and_return_valid_reminder`() = runBlocking {
        // get a reminder
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            39.0,
            41.6
        )
        remindersLocalRepository.saveReminder(reminder)

        // insert the reminder by id from the database
        val loaded = (remindersLocalRepository.getReminder(reminder.id) as Result.Success).data
        // loaded data contains the expected values
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.location, `is`(reminder.location))
    }

    @Test
    fun getReminderById_return_null_when_all_reminders_are_deleted() = runBlocking {
        // get a reminder
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            29.0,
            30.4
        )

        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().deleteAllReminders()

        val id = reminder.id
        val result = remindersDatabase.reminderDao().getReminderById(id)

        assertThat(result, `is`(CoreMatchers.nullValue()))
    }

    @Test
    fun getReminder_return_error_when_all_reminders_are_deleted() = runBlocking {
        // get a reminder
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            29.0,
            30.4
        )

        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().deleteAllReminders()

        val id = reminder.id

        val result = remindersLocalRepository.getReminder(id) as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }

    @After
    fun closeDatabase() = remindersDatabase.close()
}