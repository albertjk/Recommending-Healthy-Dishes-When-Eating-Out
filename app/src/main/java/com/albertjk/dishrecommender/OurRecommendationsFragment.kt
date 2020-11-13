package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_our_recommendations.*

class OurRecommendationsFragment : Fragment(), View.OnClickListener {

    private val TAG = OurRecommendationsFragment::class.qualifiedName

    private lateinit var navController: NavController

    // This list will store all the data associated with the scanned and identified dish names.
    private lateinit var identifiedDishes: ArrayList<Dish>

    // This list will store the user's saved recommendation preferences and priorities retrieved from the database.
    private var preferences: ArrayList<SavedRecommendationPreference>? = null

    private var recommendedDishesRecyclerView: RecyclerView? = null

    /* This boolean is used to indicate whether the generally healthy preference was selected,
    so the calculated healthiness scores will be displayed in the recycler view rows. */
    private var showHealthinessScores = false

    private lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the names of the identified dishes.
        identifiedDishes =
            arguments?.getParcelableArrayList<Dish>("identifiedDishes") as ArrayList<Dish>

        Log.d(TAG, "Received identifiedDishes: $identifiedDishes")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_our_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation visible again after the user has navigated back from viewing the bar chart.
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        startNewRecommendationProcessButton.setOnClickListener(this)

        recommendedDishesRecyclerView = view.findViewById(R.id.recommendedDishesRecyclerView)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Get the user's saved dish recommendation preferences and priorities from the database.
        preferences = (viewModel.savedPreferences.value as ArrayList<SavedRecommendationPreference>?)!!

        Log.d(TAG, "preferences: $preferences")

        // If there are multiple selected and saved preferences, sort them by priority.
        if (preferences!!.size > 1) {
            preferences!!.sortWith(compareBy { it.selectedPriority })
        }

        /* If the generally healthy recommendation preference had been selected by the user,
        set showHealthinessScores to true so that the scores will be shown in the recycler view rows. */
        for (pref in preferences!!) {
            if(pref.preferenceName == RecommendationPreferenceNameConstants.GENERALLY_HEALTHY) {
                showHealthinessScores = true
                break
            }
        }

        // Show what recommendation preferences and priorities the user had selected sorted by priority number.
        var preferencesAndPrioritiesString = ""

        for (pref in preferences!!) {
            preferencesAndPrioritiesString += "\nPreference: " + pref.preferenceName + ". Priority: " + pref.selectedPriority
        }
        selectedRecommendationPreferences.text =
            activity?.resources!!.getString(R.string.selectedPreferencesAndPriorities) + preferencesAndPrioritiesString

        initRecyclerView(showHealthinessScores)
    }

    /**
     * Sets the recycler view's layout manager and adapter.
     * The showHealthinessScores boolean is passed to the RecommendedDishesAdapter to indicate
     * whether healthiness scores should be displayed in the recycler view rows or not.
     */
    private fun initRecyclerView(showHealthinessScores: Boolean) {
        Log.d(
            TAG,
            "identifiedDishes before displaying in recycler view: $identifiedDishes"
        )

        val recommendedDishesAdapter =
            RecommendedDishesAdapter(identifiedDishes, showHealthinessScores)

        recommendedDishesRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        recommendedDishesRecyclerView!!.adapter = recommendedDishesAdapter

        // Add divider item decorations between the recycler view's rows.
        recommendedDishesRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                recommendedDishesRecyclerView!!.context,
                (recommendedDishesRecyclerView!!.layoutManager as LinearLayoutManager).orientation
            )
        )
    }

    /**
     * Handles the click event for startNewRecommendationProcessButton.
     */
    override fun onClick(v: View?) {
        when (v) {
            startNewRecommendationProcessButton -> navController.navigate(
                R.id.action_ourRecommendationsFragment_to_initiateScanningDishNamesFragment
            )
        }
    }
}