package com.albertjk.dishrecommender

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private var leftAnim : Animation? = null
    private var rightAnim : Animation? = null

    // Represents a four second delay to move from the splash screen to the Home screen.
    private val SPLASH_SCREEN_DURATION : Long = 4000

    private lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation disappear from the screen until the splash screen is visible.
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.GONE
        }

        // Hide the status bar.
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Assign animations to the application's logo and the title.
        leftAnim = AnimationUtils.loadAnimation(activity, R.anim.left_animation_splash)
        rightAnim = AnimationUtils.loadAnimation(activity, R.anim.right_animation_splash)
        appLogo!!.animation = leftAnim
        appTitleText!!.animation = rightAnim

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // After the 4-second duration, move to HomeFragment.
        Handler().postDelayed({
            navController.navigate(R.id.action_mainFragment_to_homeFragment)
        }, SPLASH_SCREEN_DURATION)
    }
}