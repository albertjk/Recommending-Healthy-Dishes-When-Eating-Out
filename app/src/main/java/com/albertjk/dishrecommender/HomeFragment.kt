package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), View.OnClickListener {

    private val TAG = HomeFragment::class.qualifiedName

    private lateinit var navController : NavController

    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation visible as the splash screen has navigated the user to the Home screen.
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        // Show the status bar since the splash screen has navigate to this Home screen.
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // Assign click listeners to the buttons.
        iAmNewButton.setOnClickListener(this)
        getRecommendationsButtonInHomeFragment.setOnClickListener(this)
    }

    /**
     * The ViewModel of MainActivity is used here.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!
    }

    /**
     * Defines which fragments to go to, upon button click, using the actions from the navigation graph.
     */
    override fun onClick(v: View?) {
        when(v) {
            iAmNewButton -> navController.navigate(R.id.action_homeFragment_to_tutorialFragment)
            getRecommendationsButtonInHomeFragment -> {

                /* Check if the user has already selected and saved recommendation preferences.
                If so, proceed to InitiateScanningDishNamesFragment. */
                val preferences = viewModel.savedPreferences.value!!

                Log.d(TAG, "viewModel.allSavedPreferences.value: " + viewModel.savedPreferences.value)

                if(preferences.isNotEmpty()) {
                    navController.navigate(R.id.action_homeFragment_to_initiateScanningDishNamesFragment)
                }

                /* Otherwise, the user must select recommendation preferences and save them.
                So, redirect to PreferencesFragment. */
                else {
                    val bundle = bundleOf(
                        "noSavedPreferencesYet" to true
                    )
                    navController.navigate(
                        R.id.action_homeFragment_to_preferencesFragment,
                        bundle
                    )
                }
            }
        }
    }
}