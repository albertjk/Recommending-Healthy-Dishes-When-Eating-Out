package com.albertjk.dishrecommender

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DishDao {

    /***
     * Inserts all dishes to the Dish table.
     */
    @Insert
    suspend fun insertAllDishes(dishes: List<Dish>)

    /**
     * Returns all dishes.
     */
    @Query("SELECT * FROM Dish")
    fun getAllDishes(): LiveData<List<Dish>>

    /**
     * Updates a Dish instance.
     */
    @Update
    suspend fun updateDish(dish: Dish)

    /**
     * Returns all dishes which are the user's favourites.
     */
    @Query("SELECT * From Dish WHERE isFavourite = 1")
    fun getFavouriteDishes(): LiveData<List<Dish>>

    /**
     * Deletes all dish instances from the Dish entity.
     */
    @Query("DELETE FROM Dish")
    suspend fun deleteAllDishes()
}