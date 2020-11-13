package com.albertjk.dishrecommender

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_initiate_scanning_dish_names.*
import java.io.Serializable

class InitiateScanningDishNamesFragment : Fragment(), View.OnClickListener, Serializable {

    private val TAG = InitiateScanningDishNamesFragment::class.qualifiedName

    private lateinit var navController: NavController

    private var identifiedDishNames: ArrayList<String>? = null

    private var scannedDishNamesRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        identifiedDishNames = ArrayList()

        // If some already identified dish names were received, then add them to the list of identifiedDishNames.
        if (arguments?.getStringArrayList("identifiedDishNames") != null) {
            arguments?.getStringArrayList("identifiedDishNames")?.let {
                identifiedDishNames!!.addAll(
                    it
                )
            }
        }
        Log.d(TAG, "received identifiedDishNames: " + identifiedDishNames.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initiate_scanning_dish_names, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make the bottom navigation visible as the crop image screen has navigated the user to the InitiateScanningDishNames screen.
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // Assign click listeners to the buttons.
        addDishNameButton.setOnClickListener(this)
        getRecommendationsButtonInInitiateScanningDishNamesFragment.setOnClickListener(this)
        clearAllDishNamesButton.setOnClickListener(this)

        scannedDishNamesRecyclerView = view.findViewById(R.id.scannedDishNamesRecyclerView)
        initRecyclerView()
    }

    /**
     * Sets the recycler view's layout manager and adapter.
     */
    private fun initRecyclerView() {
        Log.d(
            TAG,
            "Identified dish names before displaying in recycler view: $identifiedDishNames"
        )

        val scannedDishNamesAdapter = identifiedDishNames?.let {
            ScannedDishNamesAdapter(it)
        }
        scannedDishNamesRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        scannedDishNamesRecyclerView!!.adapter = scannedDishNamesAdapter

        // Add divider item decorations between the recycler view's rows.
        scannedDishNamesRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                scannedDishNamesRecyclerView!!.context,
                (scannedDishNamesRecyclerView!!.layoutManager as LinearLayoutManager).orientation
            )
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            addDishNameButton -> {
                // Redirect to the Camera Fragment. Pass the scanned dish names in a bundle.
                val bundle = bundleOf(
                    "identifiedDishNames" to identifiedDishNames
                )
                navController.navigate(
                    R.id.action_initiateScanningDishNamesFragment_to_cameraFragment,
                    bundle
                )
            }
            /* If the user clicks on the Clear All Dish Names button, check if the dish name list
            is empty, and show a dialog asking them to confirm their action. */
            clearAllDishNamesButton -> {
                if(identifiedDishNames?.isEmpty()!!) {
                    Toast.makeText(
                        activity,
                        "The dish names list is already empty.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dialogBuilder = AlertDialog.Builder(this.context)

                    dialogBuilder.setTitle("Are you sure you want to delete all dish names from the list?")

                    // Cancel button click listener
                    dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    // Positive button click listener
                    dialogBuilder.setPositiveButton("Delete") { dialog, _ ->
                        identifiedDishNames?.clear()
                        initRecyclerView() // Re-initialise the recycler view after the dish name list was cleared.
                        dialog.dismiss()
                    }
                    dialogBuilder.create().show()
                }
            }
            getRecommendationsButtonInInitiateScanningDishNamesFragment -> {

                /* If some dish names were added to the list, redirect to GeneratingRecommendationsFragment.
                Pass the dish names list in a bundle. */
                if (identifiedDishNames?.isNotEmpty()!!) {
                    val bundle = bundleOf(
                        "identifiedDishNames" to identifiedDishNames
                    )
                    navController.navigate(
                        R.id.action_initiateScanningDishNamesFragment_to_generatingRecommendationsFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(
                        activity,
                        "The dish names list is empty.\nPlease add some dish names.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}