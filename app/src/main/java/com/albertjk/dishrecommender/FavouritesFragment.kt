package com.albertjk.dishrecommender

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_favourites.*


class FavouritesFragment : Fragment(), View.OnClickListener {

    private val TAG = FavouritesFragment::class.qualifiedName

    private lateinit var navController: NavController

    private lateinit var viewModel: ViewModel

    private lateinit var noFavouritesTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation visible after CropImageFragment has navigated the user to this screen.
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        addNewFavouriteFAB.setOnClickListener(this)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Set the recycler view's layout manager and adapter.
        val favouriteDishesRecyclerView: RecyclerView = view.findViewById(R.id.favouriteDishesRecyclerView)
        val favouriteDishesAdapter = activity?.let { FavouriteDishesAdapter(it, viewModel) }
        favouriteDishesRecyclerView.layoutManager = LinearLayoutManager(activity)
        favouriteDishesRecyclerView.adapter = favouriteDishesAdapter

        // Add divider item decorations between the recycler view rows.
        val dividerItemDecoration = DividerItemDecoration(
            favouriteDishesRecyclerView.context,
            (favouriteDishesRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        favouriteDishesRecyclerView.addItemDecoration(dividerItemDecoration)

        noFavouritesTextView = TextView(this.context)

        /* Access the LiveData object. It will be scoped to the lifecycle of the fragment's view.
        Get all favourite dishes from the database. */
        viewModel.favouriteDishes.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "it: $it")

            // If there are no favourite dishes, show a text view.
            if (it.isEmpty()) {
                Log.d(TAG, "Show no favourites")
                showNoFavouritesTextView()
            } else {
                favouriteDishesConstraintLayout.removeView(noFavouritesTextView)
            }

            // Update the cached copy of the favourite dishes in the adapter.
            favouriteDishesAdapter!!.setFavouriteDishes(it)
        })
    }

    /**
     * Adds a text view, saying that there are no favourite dishes yet,
     * below the screen title, which is another text view.
     */
    private fun showNoFavouritesTextView() {
        noFavouritesTextView.text = "You haven't added any favourites yet!"

        TextViewCompat.setTextAppearance(noFavouritesTextView, android.R.style.TextAppearance_Material_Body2)

        // Set the left, top, right, and bottom padding.
        noFavouritesTextView.setPadding(
            12.pxToDp(this.context),
            64.pxToDp(this.context),
            12.pxToDp(this.context),
            64.pxToDp(this.context)
        )

        // Set the text view's width and height.
        noFavouritesTextView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT, // Width
            LayoutParams.WRAP_CONTENT  // Height
        )

        // Generate a view ID for the text view.
        noFavouritesTextView.id = View.generateViewId()

        // Add the text view to the constraint layout.
        favouriteDishesConstraintLayout.addView(noFavouritesTextView)

        // Initialise a new constraint set.
        val constraintSet = ConstraintSet()
        constraintSet.clone(favouriteDishesConstraintLayout)

        // Place the text view below the screen title.
        constraintSet.connect(noFavouritesTextView.id, ConstraintSet.TOP, R.id.favouritesScreenTitle, ConstraintSet.BOTTOM)
        constraintSet.connect(noFavouritesTextView.id, ConstraintSet.START, R.id.favouriteDishesConstraintLayout, ConstraintSet.START)
        constraintSet.connect(noFavouritesTextView.id, ConstraintSet.END, R.id.favouriteDishesConstraintLayout, ConstraintSet.END)

        // Apply the constraint set to the constraint layout.
        constraintSet.applyTo(favouriteDishesConstraintLayout)
    }

    override fun onClick(v: View?) {
        when (v) {
            addNewFavouriteFAB -> {
                val bundle = bundleOf(
                    "isBeingAddedAsFavourite" to true
                )
                navController.navigate(
                    R.id.action_favouritesFragment_to_cameraFragment,
                    bundle
                )
            }
        }
    }

    /**
     * Converts pixel units to dp units and returns these dp units.
     */
    private fun Int.pxToDp(context: Context?): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context?.resources?.displayMetrics
    ).toInt()
}