package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDatabase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries().build()
    }

    @Test
    fun insertReminder() = runBlocking {
        // get a reminder
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            39.0,
            41.6
        )
        remindersDatabase.reminderDao().saveReminder(reminder)

        // insert the reminder by id from the database
        val loaded = remindersDatabase.reminderDao().getReminderById(reminder.id)
        // loaded data contains the expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.location, `is`(reminder.location))
    }

    @Test
    fun noReminderForDeleted() = runBlocking {
        // get a reminder
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            29.0,
            30.4)

        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().deleteAllReminders()

        val id = reminder.id
        val result = remindersDatabase.reminderDao().getReminderById(id)

        assertThat(result, `is`(nullValue()))
    }

    @After
    fun closeDatabase() = remindersDatabase.close()
}