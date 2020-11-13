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
class BreakfastLogDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and BreakfastLogDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: BreakfastLogDao

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

        dao = database.breakfastLogDao()
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
     * Tests if the insertBreakfastLog function of BreakfastLogDao works as expected.
     */
    @Test
    fun insertBreakfastLog() = runBlockingTest {

        // Create an example breakfast log and insert it into the database.
        val breakfastLog = BreakfastLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertBreakfastLog(breakfastLog)

        // Get the breakfast log list from the database.
        val allBreakfastLogs = dao.getBreakfastLogs().getOrAwaitValue()

        // Check if the breakfast log is in the list.
        assertThat(allBreakfastLogs).contains(breakfastLog)
    }

    /**
     * Tests if the updateBreakfastLog function of BreakfastLogDao works as expected.
     */
    @Test
    fun updateBreakfastLog() = runBlockingTest {

        // Create an example breakfast log and insert it into the database.
        val breakfastLog = BreakfastLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertBreakfastLog(breakfastLog)

        // Then, update the breakfast log in the database.
        val breakfastLogUpdated = BreakfastLog(Date(2020, 9, 27), 4, 34f, 42f, 52f, 62f, 72f, 82f, 92f)
        dao.updateBreakfastLog(breakfastLogUpdated)

        // Get the breakfast log list from the database.
        val allBreakfastLogs = dao.getBreakfastLogs().getOrAwaitValue()

        // Check if the list contains the updated breakfast log.
        assertThat(allBreakfastLogs).contains(breakfastLogUpdated)
    }

    /**
     * Tests if the deleteBreakfastLog function of BreakfastLogDao works as expected.
     */
    @Test
    fun deleteBreakfastLog() = runBlockingTest {

        // Create an example breakfast log and insert it into the database.
        val breakfastLog = BreakfastLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertBreakfastLog(breakfastLog)

        // Delete the breakfast log.
        dao.deleteBreakfastLog(breakfastLog)

        // Get the breakfast log list from the database.
        val allBreakfastLogs = dao.getBreakfastLogs().getOrAwaitValue()

        // Check if the breakfast log list is empty as the item was deleted.
        assertThat(allBreakfastLogs).doesNotContain(breakfastLog)
    }
}