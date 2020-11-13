package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData

/**
 * Declares the DAOs as private properties in the constructor.
 * Accesses the methods of the DAOs.
 */
class Repository(private val dishDao: DishDao, private val savedRecommendationPreferenceDao: SavedRecommendationPreferenceDao, private val dishLogDao: DishLogDao, private val dailyLogDao: DailyLogDao, private val breakfastLogDao: BreakfastLogDao, private val lunchLogDao: LunchLogDao, private val dinnerLogDao: DinnerLogDao) {

    /**
     * Updates a specific dish.
     */
    suspend fun updateDish(dish: Dish) {
        dishDao.updateDish(dish)
    }

    // Returns all dishes in the Dish table.
    val allDishes: LiveData<List<Dish>> = dishDao.getAllDishes()

    // Returns all dishes in the Dish table that are the user's favourites.
    val favouriteDishes: LiveData<List<Dish>> = dishDao.getFavouriteDishes()

    /**
     * Inserts an instance to the DailyLog table.
     */
    suspend fun insertDailyLog(dailyLog: DailyLog) {
        dailyLogDao.insertDailyLog(dailyLog)
    }

    /**
     * Updates an instance in the DailyLog table.
     */
    suspend fun updateDailyLog(dailyLog: DailyLog) {
        dailyLogDao.updateDailyLog(dailyLog)
    }

    /**
     * Removes an instance from the DailyLog table.
     */
    suspend fun deleteDailyLog(dailyLog: DailyLog) {
        dailyLogDao.deleteDailyLog(dailyLog)
    }

    // Returns every entry in the DailyLog entity.
    val dailyLogs: LiveData<List<DailyLog>> = dailyLogDao.getDailyLogs()

    // Returns every entry in the BreakfastLog entity.
    val breakfastLogs: LiveData<List<BreakfastLog>> = breakfastLogDao.getBreakfastLogs()

    /**
     * Inserts an instance to the BreakfastLog table.
     */
    suspend fun insertBreakfastLog(breakfastLog: BreakfastLog) {
        breakfastLogDao.insertBreakfastLog(breakfastLog)
    }

    /**
     * Updates a BreakfastLog instance.
     */
    suspend fun updateBreakfastLog(breakfastLog: BreakfastLog) {
        breakfastLogDao.updateBreakfastLog(breakfastLog)
    }

    /**
     * Removes an instance from the BreakfastLog table.
     */
    suspend fun deleteBreakfastLog(breakfastLog: BreakfastLog) {
        breakfastLogDao.deleteBreakfastLog(breakfastLog)
    }

    // Returns every entry in the LunchLog entity.
    val lunchLogs: LiveData<List<LunchLog>> = lunchLogDao.getLunchLogs()

    /**
     * Inserts an instance to the LunchLog table.
     */
    suspend fun insertLunchLog(lunchLog: LunchLog) {
        lunchLogDao.insertLunchLog(lunchLog)
    }

    /**
     * Updates a LunchLog instance.
     */
    suspend fun updateLunchLog(lunchLog: LunchLog) {
        lunchLogDao.updateLunchLog(lunchLog)
    }

    /**
     * Removes an instance from the LunchLog table.
     */
    suspend fun deleteLunchLog(lunchLog: LunchLog) {
        lunchLogDao.deleteLunchLog(lunchLog)
    }

    // Returns every entry in the DinnerLog entity.
    val dinnerLogs: LiveData<List<DinnerLog>> = dinnerLogDao.getDinnerLogs()

    /**
     * Inserts an instance to the DinnerLog table.
     */
    suspend fun insertDinnerLog(dinnerLog: DinnerLog) {
        dinnerLogDao.insertDinnerLog(dinnerLog)
    }

    /**
     * Updates a DinnerLog instance.
     */
    suspend fun updateDinnerLog(dinnerLog: DinnerLog) {
        dinnerLogDao.updateDinnerLog(dinnerLog)
    }

    /**
     * Removes an instance from the DinnerLog table.
     */
    suspend fun deleteDinnerLog(dinnerLog: DinnerLog) {
        dinnerLogDao.deleteDinnerLog(dinnerLog)
    }

    /**
     * Inserts a list of instances to the SavedRecommendationPreference table.
     */
    suspend fun insertAllRecommendationPreferences(savedRecommendationPreferences: List<SavedRecommendationPreference>) {
        savedRecommendationPreferenceDao.insertAllRecommendationPreferences(savedRecommendationPreferences)
    }

    // Returns every entry in the SavedRecommendationPreference entity.
    val savedRecommendationPreferences: LiveData<List<SavedRecommendationPreference>> = savedRecommendationPreferenceDao.getRecommendationPreferences()

    /**
     * Removes all instances in the SavedRecommendationPreference entity.
     */
    suspend fun deleteAllRecommendationPreferences() {
        savedRecommendationPreferenceDao.deleteAllRecommendationPreferences()
    }

    /**
     * Inserts an instance to the DishLog table.
     */
    suspend fun insertDishLog(dishLog: DishLog) {
        dishLogDao.insertDishLog(dishLog)
    }

    // Returns every entry in the DishLog entity.
    val dishLogs: LiveData<List<DishLog>> = dishLogDao.getDishLogs()

    /**
     * Removes an instance from the DishLog table.
     */
    suspend fun deleteDishLog(dishLog: DishLog) {
        dishLogDao.deleteDishLog(dishLog)
    }
}