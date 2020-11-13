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
class LunchLogDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and LunchLogDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: LunchLogDao

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

        dao = database.lunchLogDao()
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
     * Tests if the insertLunchLog function of LunchLogDao works as expected.
     */
    @Test
    fun insertLunchLog() = runBlockingTest {

        // Create an example lunch log and insert it into the database.
        val lunchLog = LunchLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertLunchLog(lunchLog)

        // Get the lunch log list from the database.
        val allLunchLogs = dao.getLunchLogs().getOrAwaitValue()

        // Check if the lunch log is in the list.
        assertThat(allLunchLogs).contains(lunchLog)
    }

    /**
     * Tests if the updateLunchLog function of LunchLogDao works as expected.
     */
    @Test
    fun updateLunchLog() = runBlockingTest {

        // Create an example lunch log and insert it into the database.
        val lunchLog = LunchLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertLunchLog(lunchLog)

        // Then, update the lunch log in the database.
        val lunchLogUpdated = LunchLog(Date(2020, 9, 27), 4, 34f, 42f, 52f, 62f, 72f, 82f, 92f)
        dao.updateLunchLog(lunchLogUpdated)

        // Get the lunch log list from the database.
        val allLunchLogs = dao.getLunchLogs().getOrAwaitValue()

        // Check if the list contains the updated lunch log.
        assertThat(allLunchLogs).contains(lunchLogUpdated)
    }

    /**
     * Tests if the deleteLunchLog function of LunchLogDao works as expected.
     */
    @Test
    fun deleteLunchLog() = runBlockingTest {

        // Create an example lunch log and insert it into the database.
        val lunchLog = LunchLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertLunchLog(lunchLog)

        // Delete the lunch log.
        dao.deleteLunchLog(lunchLog)

        // Get the lunch log list from the database.
        val allLunchLogs = dao.getLunchLogs().getOrAwaitValue()

        // Check if the lunch log list is empty as the item was deleted.
        assertThat(allLunchLogs).doesNotContain(lunchLog)
    }
}