package com.albertjk.dishrecommender

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DailyLogDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and DailyLogDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: DailyLogDao

    /**
     * This function is executed before each test case in this class.
     */
    @Before
    fun setup() {
        /* Create the database in memory before each test function.
        It will not be held in the persistent storage.
        The database will only be saved for a test case. */
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.dailyLogDao()
    }

    /**
     * This function is executed after each test case in this class.
     */
    @After
    fun teardown() {
        // Close the database after a test case.
        database.close()
    }

    /**
     * Tests if the insertDailyLog function of DailyLogDao works as expected.
     */
    @Test
    fun insertDailyLog() = runBlockingTest {

        // Create an example daily log and insert it into the database.
        val dailyLog = DailyLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDailyLog(dailyLog)

        // Get the daily log list from the database.
        val allDailyLogs = dao.getDailyLogs().getOrAwaitValue()

        // Check if the daily log is in the list.
        assertThat(allDailyLogs).contains(dailyLog)
    }

    /**
     * Tests if the updateDailyLog function of DailyLogDao works as expected.
     */
    @Test
    fun updateDailyLog() = runBlockingTest {

        // Create an example daily log and insert it into the database.
        val dailyLog = DailyLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDailyLog(dailyLog)

        // Then, update the daily log in the database.
        val dailyLogUpdated = DailyLog(Date(2020, 9, 27), 4, 34f, 42f, 52f, 62f, 72f, 82f, 92f)
        dao.updateDailyLog(dailyLogUpdated)

        // Get the daily log list from the database.
        val allDailyLogs = dao.getDailyLogs().getOrAwaitValue()

        // Check if the list contains the updated daily log.
        assertThat(allDailyLogs).contains(dailyLogUpdated)
    }

    /**
     * Tests if the deleteDailyLog function of DailyLogDao works as expected.
     */
    @Test
    fun deleteDailyLog() = runBlockingTest {

        // Create an example daily log and insert it into the database.
        val dailyLog = DailyLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDailyLog(dailyLog)

        // Delete the daily log.
        dao.deleteDailyLog(dailyLog)

        // Get the daily log list from the database.
        val allDailyLogs = dao.getDailyLogs().getOrAwaitValue()

        // Check if the daily log list is empty as the item was deleted.
        assertThat(allDailyLogs).doesNotContain(dailyLog)
    }
}