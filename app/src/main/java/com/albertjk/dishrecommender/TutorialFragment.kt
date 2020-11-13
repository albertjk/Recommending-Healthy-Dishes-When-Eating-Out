package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_tutorial.*

class TutorialFragment : Fragment(), View.OnClickListener, ViewPager.OnPageChangeListener {

    private val TAG = TutorialFragment::class.qualifiedName

    private lateinit var navController: NavController

    private lateinit var viewModel: ViewModel

    // This variable will store the number of dots to be displayed underneath the ViewPager.
    private var dotCount = 0

    // This array list will store the dot drawable resource files as ImageViews.
    private lateinit var dots: ArrayList<ImageView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // Specify a callback to be invoked when the getStartedFromTutorialButton is clicked.
        getRecommendationsButtonInTutorialFragment.setOnClickListener(this)

        val viewPagerAdapter = activity?.let { ViewPagerAdapter(it) }
        viewPager.adapter = viewPagerAdapter

        dotCount = viewPagerAdapter!!.count
        dots = ArrayList()

        // Add the dots below the ViewPager on the screen.
        for (i in 0 until dotCount) {
            val imageView = ImageView(context)
            imageView.setImageDrawable(activity?.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.tutorial_inactive_dot
                )
            })
            dots.add(imageView)

            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Set the left and right margins around a dot to 10 pixels.
            params.setMargins(10, 0, 10, 0)

            sliderDots.addView(dots[i], params)
        }
        // The first image will be displayed first, so the first dot from the left will be an active dot.
        dots[0].setImageDrawable(activity?.let {
            ContextCompat.getDrawable(
                it,
                R.drawable.tutorial_active_dot
            )
        })

        // Set a callback for responding to the changing state of the selected page (the selected image).
        viewPager.addOnPageChangeListener(this)
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
     * After scrolling horizontally to a given image, that image becomes selected.
     * Then, this method sets the dot associated with that image (that position) active,
     * and all other dots inactive.
     */
    override fun onPageSelected(position: Int) {
        for (i in 0 until dotCount) {
            dots[i].setImageDrawable(activity?.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.tutorial_inactive_dot
                )
            })
        }
        dots[position].setImageDrawable(activity?.let {
            ContextCompat.getDrawable(
                it,
                R.drawable.tutorial_active_dot
            )
        })
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    /**
     * Defines the click event for the getRecommendationsButtonInTutorialFragment.
     */
    override fun onClick(v: View?) {
        when (v) {

            getRecommendationsButtonInTutorialFragment -> {
                Log.d(TAG, "viewModel.allSavedPreferences.value: " + viewModel.savedPreferences.value)

                /* Check if the user has already selected and saved recommendation preferences.
                If so, proceed to InitiateScanningDishNamesFragment. */
                Log.d(TAG, "viewModel.allSavedPreferences.value: " + viewModel.savedPreferences.value)
                if(viewModel.savedPreferences.value!!.isNotEmpty()) {
                    navController.navigate(R.id.action_tutorialFragment_to_initiateScanningDishNamesFragment)
                }
                /* Otherwise, the user must select recommendation preferences and save them.
                So, redirect to PreferencesFragment. */
                else {
                    val bundle = bundleOf(
                        "noSavedPreferencesYet" to true
                    )
                    navController.navigate(
                        R.id.action_tutorialFragment_to_preferencesFragment,
                        bundle
                    )
                }
            }
        }
    }
}