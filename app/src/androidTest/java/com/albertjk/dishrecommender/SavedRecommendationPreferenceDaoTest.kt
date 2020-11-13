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
class SavedRecommendationPreferenceDaoTest {

    // This rule specifies that the test functions are to be executed one after the other.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // References to the database and SavedRecommendationPreferenceDao.
    private lateinit var database: AppDatabase
    private lateinit var dao: SavedRecommendationPreferenceDao

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

        dao = database.recommendationPreferenceDao()
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
     * Tests if the insertAllRecommendationPreferences function of SavedRecommendationPreferenceDao works as expected.
     */
    @Test
    fun insertAllRecommendationPreferences() = runBlockingTest {

        // Create an example recommendation preference, put it in a list, and insert it into the database.
        val preference = SavedRecommendationPreference(1, "preferenceName", 1)
        dao.insertAllRecommendationPreferences(mutableListOf(preference))

        // Get the recommendation preference list from the database.
        val allPreferences = dao.getRecommendationPreferences().getOrAwaitValue()

        // Check if the recommendation preference is in the list.
        assertThat(allPreferences).contains(preference)
    }

    /**
     * Tests if the deleteAllRecommendationPreferences function of SavedRecommendationPreferenceDao works as expected.
     */
    @Test
    fun deleteAllRecommendationPreferences() = runBlockingTest {

        // Create an example recommendation preference, put it in a list, and insert it into the database.
        val preference = SavedRecommendationPreference(1, "preferenceName", 1)
        dao.insertAllRecommendationPreferences(mutableListOf(preference))

        // Delete the recommendation preference.
        dao.deleteAllRecommendationPreferences()

        // Get the recommendation preference list from the database.
        val allPreferences = dao.getRecommendationPreferences().getOrAwaitValue()

        // Check if the recommendation preference list is empty as the item was deleted.
        assertThat(allPreferences).doesNotContain(preference)
    }
}