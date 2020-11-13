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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DishDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and DishDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: DishDao

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

        dao = database.dishDao()
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
     * Tests if the insertAllDishes function of DishDao works as expected.
     */
    @Test
    fun insertAllDishes() = runBlockingTest {

        // Create an example dish, put it in a list, and insert it into the database.
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dao.insertAllDishes(mutableListOf(dish))

        // Get the dish list from the database.
        val allDishes = dao.getAllDishes().getOrAwaitValue()

        // Check if the dish is in the list.
        assertThat(allDishes).contains(dish)
    }

    /**
     * Tests if the deleteAllDishes function of DishDao works as expected.
     */
    @Test
    fun deleteAllDishes() = runBlockingTest {

        // Create an example dish, put it in a list, and insert it into the database.
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dao.insertAllDishes(mutableListOf(dish))

        // Delete the dish.
        dao.deleteAllDishes()

        // Get the dish list from the database.
        val allDishes = dao.getAllDishes().getOrAwaitValue()

        // Check if the dish list is empty as the item was deleted.
        assertThat(allDishes).doesNotContain(dish)
    }

    /**
     * Tests if the updateDish function of DishDao works as expected.
     */
    @Test
    fun updateDish() = runBlockingTest {

        // Create an example dish, put it in a list, and insert it into the database.
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dao.insertAllDishes(mutableListOf(dish))

        // Then, update the dish in the database.
        val dishUpdated = Dish(1, "updatedDish", 2, 4.5f, 7.52f, 58.64f, 76.85f, 51.51f, 83.83f, 11.11f, 1)
        dao.updateDish(dishUpdated)

        // Get the dish list from the database.
        val allDishes = dao.getAllDishes().getOrAwaitValue()

        // Check if the list contains the updated dish.
        assertThat(allDishes).contains(dishUpdated)
    }

    /**
     * Tests if the getFavouriteDishes function of DishDao works as expected.
     */
    @Test
    fun getFavouriteDishes() = runBlockingTest {

        // Create an example dish, put it in a list, and insert it into the database.
        val dish = Dish(1, "dishName", 1, 3.4f, 1.5f, 5.6f, 7.8f, 5.5f, 8.8f, 1.1f, 0)
        dao.insertAllDishes(mutableListOf(dish))

        // Get the favourite dishes from the database and store them in a list.
        var favourites = dao.getFavouriteDishes().getOrAwaitValue()

        // Check if there are no favourites.
        assertThat(favourites).doesNotContain(dish)

        // Now add another dish to the database.
        val anotherDish = Dish(2, "dishName2", 1213, 3.432f, 143.5f, 54.6f, 47.83f, 53.5f, 83.8f, 13.1f, 1)
        dao.insertAllDishes(mutableListOf(anotherDish))

        // Get the favourite dishes from the database and store them in a list.
        favourites = dao.getFavouriteDishes().getOrAwaitValue()

        // Check if there is a favourite dish in the list.
        assertThat(favourites).contains(anotherDish)
    }
}