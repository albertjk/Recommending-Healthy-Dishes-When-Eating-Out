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
class DinnerLogDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and DinnerLogDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: DinnerLogDao

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

        dao = database.dinnerLogDao()
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
     * Tests if the insertDinnerLog function of DinnerLogDao works as expected.
     */
    @Test
    fun insertDinnerLog() = runBlockingTest {

        // Create an example dinner log and insert it into the database.
        val dinnerLog = DinnerLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDinnerLog(dinnerLog)

        // Get the dinner log list from the database.
        val allDinnerLogs = dao.getDinnerLogs().getOrAwaitValue()

        // Check if the dinner log is in the list.
        assertThat(allDinnerLogs).contains(dinnerLog)
    }

    /**
     * Tests if the updateDinnerLog function of DinnerLogDao works as expected.
     */
    @Test
    fun updateDinnerLog() = runBlockingTest {

        // Create an example dinner log and insert it into the database.
        val dinnerLog = DinnerLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDinnerLog(dinnerLog)

        // Then, update the dinner log in the database.
        val dinnerLogUpdated = DinnerLog(Date(2020, 9, 27), 4, 34f, 42f, 52f, 62f, 72f, 82f, 92f)
        dao.updateDinnerLog(dinnerLogUpdated)

        // Get the dinner log list from the database.
        val allDinnerLogs = dao.getDinnerLogs().getOrAwaitValue()

        // Check if the list contains the updated dinner log.
        assertThat(allDinnerLogs).contains(dinnerLogUpdated)
    }

    /**
     * Tests if the deleteDinnerLog function of DinnerLogDao works as expected.
     */
    @Test
    fun deleteDinnerLog() = runBlockingTest {

        // Create an example dinner log and insert it into the database.
        val dinnerLog = DinnerLog(Date(2020, 9, 27), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dao.insertDinnerLog(dinnerLog)

        // Delete the dinner log.
        dao.deleteDinnerLog(dinnerLog)

        // Get the dinner log list from the database.
        val allDinnerLogs = dao.getDinnerLogs().getOrAwaitValue()

        // Check if the dinner log list is empty as the item was deleted.
        assertThat(allDinnerLogs).doesNotContain(dinnerLog)
    }
}