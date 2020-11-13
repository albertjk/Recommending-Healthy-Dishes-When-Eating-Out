package com.albertjk.dishrecommender

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* The viewModel is scoped to the lifecycle of MainActivity, by passing 'this'
        as the lifecycle owner to both the ViewModel and the LiveData. */
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.allDishes.observe(this, Observer { })
        viewModel.savedPreferences.observe(this, Observer { })
        viewModel.dishLogs.observe(this, Observer { })
        viewModel.dailyLogs.observe(this, Observer { })
        viewModel.breakfastLogs.observe(this, Observer { })
        viewModel.lunchLogs.observe(this, Observer { })
        viewModel.dinnerLogs.observe(this, Observer { })

        // Initialise the bottom navigation view and the Navigation Controller and merge them.
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navController = findNavController(R.id.fragment)
        bottomNavigationView.setupWithNavController(navController)
    }
}


