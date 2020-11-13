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
class DishLogDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and DishLogDao.
    private lateinit var database: AppDatabase
    private lateinit var dishLogDao: DishLogDao
    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var dishDao: DishDao

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

        dishLogDao = database.dishLogDao()
        dailyLogDao = database.dailyLogDao()
        dishDao = database.dishDao()
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
     * Tests if the insertDishLog function of DishLogDao works as expected.
     */
    @Test
    fun insertDishLog() = runBlockingTest {

        /* Create an example dish, put it in a list, and insert it into the database
        as the DishLog entity's dishId attribute references the dishId in the Dish entity.
        So, the dish must be in the database to test the DishLog entity. */
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dishDao.insertAllDishes(mutableListOf(dish))

        /* Create an example daily log and insert it into the database
        as the DishLog entity's logDate attribute references the logDate in the DailyLog entity.
        So, the daily log must be in the database to test the DishLog entity. */
        val dailyLog = DailyLog(Date(2020, 10, 20), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dailyLogDao.insertDailyLog(dailyLog)

        // Create an example dish log and insert it into the database.
        val dishLog = DishLog(1, 1, "breakfast", Date(2020, 10, 20))
        dishLogDao.insertDishLog(dishLog)

        // Get the dish log list from the database.
        val allDishLogs = dishLogDao.getDishLogs().getOrAwaitValue()

        // Check if the dish log is in the list.
        assertThat(allDishLogs).contains(dishLog)
    }

    /**
     * Tests if the deleteDishLog function of DishLogDao works as expected.
     */
    @Test
    fun deleteDishLog() = runBlockingTest {

        /* Create an example dish, put it in a list, and insert it into the database
        as the DishLog entity's dishId attribute references the dishId in the Dish entity.
        So, the dish must be in the database to test the DishLog entity. */
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dishDao.insertAllDishes(mutableListOf(dish))

        /* Create an example daily log and insert it into the database
        as the DishLog entity's logDate attribute references the logDate in the DailyLog entity.
        So, the daily log must be in the database to test the DishLog entity. */
        val dailyLog = DailyLog(Date(2020, 10, 20), 2, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        dailyLogDao.insertDailyLog(dailyLog)

        // Create an example dish log and insert it into the database.
        val dishLog = DishLog(1, 1, "breakfast", Date(2020, 10, 20))
        dishLogDao.insertDishLog(dishLog)

        // Delete the dish log.
        dishLogDao.deleteDishLog(dishLog)

        // Get the dish log list from the database.
        val allDishLogs = dishLogDao.getDishLogs().getOrAwaitValue()

        // Check if the dish log list is empty as the item was deleted.
        assertThat(allDishLogs).doesNotContain(dishLog)
    }
}