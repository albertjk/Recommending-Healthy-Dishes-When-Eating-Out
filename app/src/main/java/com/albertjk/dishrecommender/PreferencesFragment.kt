package com.albertjk.dishrecommender

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_preferences.*

class PreferencesFragment : Fragment(),
    AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    private val TAG = PreferencesFragment::class.qualifiedName

    private lateinit var navController: NavController

    /* This HashMap will store if checkboxes are checked at the moment or not.
    It will map checkbox views to boolean values. */
    private lateinit var checkBoxesChecked: HashMap<View, Boolean>

    /* This HashMap will store if spinners are visible at the moment or not.
    It will map spinner views to boolean values. */
    private lateinit var spinnersVisible: HashMap<View, Boolean>

    // This HashMap will map checkbox views to their corresponding spinner views.
    private lateinit var checkBoxesAndSpinners: HashMap<View, View>

    // This HashMap will map checkbox strings to their corresponding checkbox views.
    private lateinit var checkBoxStringsAndViews: HashMap<String, View>

    // Keeps track of how many checkboxes are checked at the moment.
    private var numOfCheckBoxesChecked: Int = 0

    // Priority numbers between 1 and 4 will be dynamically added to this array to  fill the spinners.
    private lateinit var selectablePriorities: List<Int>

    private lateinit var viewModel: ViewModel

    private var noSavedPreferencesYet = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_preferences,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Get the noSavedPreferencesYet boolean passed to this fragment if the user was redirected from HomeFragment.
        If this boolean is false, after saving the preferences, the user will not be redirected to InitiateScanningDishNamesFragment.
        But if it is true, after saving the preferences, the user will be redirected to InitiateScanningDishNamesFragment. */
        if (arguments?.getBoolean("noSavedPreferencesYet") != null) {
            noSavedPreferencesYet = arguments?.getBoolean("noSavedPreferencesYet")!!
        }

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        checkBoxesChecked = HashMap()
        checkBoxesChecked[generallyHealthyCheckBox] = false
        checkBoxesChecked[lowCalorieCheckBox] = false
        checkBoxesChecked[lowCarbCheckBox] = false
        checkBoxesChecked[highProteinCheckBox] = false

        // Generally healthy is checked by default.
        numOfCheckBoxesChecked = 0

        spinnersVisible = HashMap()
        spinnersVisible[generallyHealthySpinner] = false
        spinnersVisible[lowCalorieSpinner] = false
        spinnersVisible[lowCarbSpinner] = false
        spinnersVisible[highProteinSpinner] = false

        checkBoxesAndSpinners = HashMap()
        checkBoxesAndSpinners[generallyHealthyCheckBox] = generallyHealthySpinner
        checkBoxesAndSpinners[lowCalorieCheckBox] = lowCalorieSpinner
        checkBoxesAndSpinners[lowCarbCheckBox] = lowCarbSpinner
        checkBoxesAndSpinners[highProteinCheckBox] = highProteinSpinner

        checkBoxStringsAndViews = HashMap()
        checkBoxStringsAndViews[RecommendationPreferenceNameConstants.GENERALLY_HEALTHY] =
            generallyHealthyCheckBox
        checkBoxStringsAndViews[RecommendationPreferenceNameConstants.LOW_CALORIES] =
            lowCalorieCheckBox
        checkBoxStringsAndViews[RecommendationPreferenceNameConstants.LOW_CARB] = lowCarbCheckBox
        checkBoxStringsAndViews[RecommendationPreferenceNameConstants.HIGH_PROTEIN] =
            highProteinCheckBox

        generallyHealthyCheckBox.setOnClickListener(this)
        lowCalorieCheckBox.setOnClickListener(this)
        lowCarbCheckBox.setOnClickListener(this)
        highProteinCheckBox.setOnClickListener(this)

        generallyHealthySpinner.onItemSelectedListener = this
        lowCalorieSpinner.onItemSelectedListener = this
        lowCarbSpinner.onItemSelectedListener = this
        highProteinSpinner.onItemSelectedListener = this

        savePreferencesButton.setOnClickListener(this)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        /* If the user has previously selected and saved some preferences, get these from the database,
        tick the relevant checkboxes, and set the priority values in the corresponding spinners. */
        val preferences = viewModel.savedPreferences.value!!

        Log.d(TAG, "viewModel.allSavedPreferences.value: $preferences")
        if (preferences.isNotEmpty()) {
            showSavedPreferencesAndPriorities(preferences)
        }
    }

    /**
     * Ticks the checkboxes and sets the corresponding priority values on the screen
     * according to the user's selected and saved preferences in the database.
     */
    private fun showSavedPreferencesAndPriorities(preferences: List<SavedRecommendationPreference>) {

        /* If there is only one saved preference, only tick the corresponding checkbox.
        No spinner is made visible in this case. */
        if (preferences.size == 1) {
            val prefString = preferences[0].preferenceName

            // Get the checkbox based on the preference string.
            val checkBox = checkBoxStringsAndViews[prefString] as CheckBox
            checkBox.isChecked = true
            checkBoxesChecked[checkBox] = true
            numOfCheckBoxesChecked = numOfCheckBoxesChecked.plus(1)

            setVisibleSpinnersToGone()
        }
        /* If there are multiple saved preferences, tick the corresponding checkboxes
        and set the corresponding priority values. */
        else if (preferences.size > 1) {

            // Set priority choices from 1 to the number of preferences saved in the spinner.
            selectablePriorities = ArrayList()
            for (i in 1..preferences.size) {
                (selectablePriorities as ArrayList<Int>).add(i)
            }

            Log.d(TAG, "selectablePriorities: $selectablePriorities")

            Log.d(TAG, "Saved preferences: $preferences")

            for (pref in preferences) {
                val prefString = pref.preferenceName
                val prefPriority = pref.selectedPriority

                Log.d(TAG, "spinnersVisible: $spinnersVisible")

                // Get the checkbox based on the preference string.
                val checkBox = checkBoxStringsAndViews[prefString] as CheckBox
                checkBox.isChecked = true
                checkBoxesChecked[checkBox] = true
                numOfCheckBoxesChecked = numOfCheckBoxesChecked.plus(1)

                prioritiesTextView.visibility = View.VISIBLE

                // Get the checkbox's corresponding spinner.
                val spinner = checkBoxesAndSpinners[checkBox] as Spinner
                spinner.visibility = View.VISIBLE

                // Create an ArrayAdapter using a simple spinner layout and the priorities array list
                val adapter = activity?.let {
                    ArrayAdapter<Int>(
                        it,
                        android.R.layout.simple_spinner_item,
                        selectablePriorities
                    )
                }

                // Set the layout to use when the list of choices appear
                adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Set the adapter for the spinner.
                spinner.adapter = adapter

                Log.d(TAG, "spinner: $spinner")

                Log.d(TAG, "priorities: $selectablePriorities")
                Log.d(TAG, "prefPriority: $prefPriority")
                Log.d(TAG, "prefPriority - 1: " + (prefPriority - 1))
                Log.d(
                    TAG,
                    "priorities[prefPriority - 1]: " + selectablePriorities[prefPriority - 1]
                )

                //  Set the saved priority value as the selected element in the spinner.
                spinner.setSelection(prefPriority - 1)

                Log.d(TAG, "Passed spinner.setSelection")

                spinnersVisible[spinner] = true
            }
        }
    }

    /**
     * Sets the spinners associated with the checked checkboxes, other than the input checkbox c, to VISIBLE.
     */
    private fun setOtherSpinnersVisible(c: CheckBox) {
        Log.d(TAG, "setOtherSpinnersVisible")

        for (checkbox in checkBoxesChecked) {

            // If the current checkbox is not checkbox c, and the current checkbox is checked.
            if (checkbox.key != c && (checkbox.key as CheckBox).isChecked) {

                // Get the spinner and set its visibility to VISIBLE.
                checkBoxesAndSpinners[checkbox.key]!!.visibility = View.VISIBLE

                // Also set in the spinnersVisible HashMap that the spinner is visible.
                spinnersVisible[checkBoxesAndSpinners[checkbox.key]!!] = true

                // Set choices from 1 to numOfCheckBoxesChecked in the spinner.
                selectablePriorities = ArrayList()
                for (i in 1..numOfCheckBoxesChecked) {
                    (selectablePriorities as ArrayList<Int>).add(i)
                }
                Log.d(TAG, "selectablePriorities: $selectablePriorities")

                // Create an ArrayAdapter using a simple spinner layout and the priorities array list
                val adapter = activity?.let {
                    ArrayAdapter<Int>(
                        it,
                        android.R.layout.simple_spinner_item,
                        selectablePriorities
                    )
                }

                // Set the layout to use when the list of choices appear
                adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Set the adapter for the spinner.
                (checkBoxesAndSpinners[checkbox.key] as Spinner).adapter = adapter
            }
        }
    }

    /**
     * Sets VISIBLE spinners to GONE.
     */
    private fun setVisibleSpinnersToGone() {
        Log.d(TAG, "setVisibleSpinnersToGone")

        for (spinner in spinnersVisible) {
            if (spinner.value) {
                spinner.key.visibility = View.GONE
                spinnersVisible[spinner.key] = false
            }
        }
    }

    /**
     * Handles the event when a checkbox is checked.
     */
    private fun handleCheckedCheckBox(c: CheckBox) {
        Log.d(TAG, "handleCheckedCheckBox")

        numOfCheckBoxesChecked = numOfCheckBoxesChecked.plus(1)
        checkBoxesChecked[c] = true

        /* If more than one checkbox is checked, find out which one, and set the priorities title
        and the spinner associated with checkbox c visible. */
        if (numOfCheckBoxesChecked > 1) {
            prioritiesTextView.visibility = View.VISIBLE
            checkBoxesAndSpinners[c]?.visibility = View.VISIBLE
            spinnersVisible[checkBoxesAndSpinners[c]!!] = true

            // Set choices from 1 to numOfCheckBoxesChecked in the spinner.
            selectablePriorities = ArrayList()
            for (i in 1..numOfCheckBoxesChecked) {
                (selectablePriorities as ArrayList<Int>).add(i)
            }
            Log.d(TAG, "selectablePriorities: $selectablePriorities")

            // Create an ArrayAdapter using a simple spinner layout and the priorities array list.
            val adapter = activity?.let {
                ArrayAdapter<Int>(
                    it,
                    android.R.layout.simple_spinner_item,
                    selectablePriorities
                )
            }

            // Set the layout to use when the list of choices appear.
            adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Set the adapter for the spinner.
            (checkBoxesAndSpinners[c] as Spinner).adapter = adapter

            // For all other checkboxes in the screen, if they are checked, make their spinners visible.
            setOtherSpinnersVisible(c)
        }
    }

    /**
     * Handles the event when a checkbox is unchecked.
     */
    private fun handleUncheckedCheckBox(c: CheckBox) {
        Log.d(TAG, "handleUncheckedCheckBox")

        numOfCheckBoxesChecked = numOfCheckBoxesChecked.minus(1)
        checkBoxesChecked[c] = false

        /* If more than one checkbox is checked, find out which one, and set the priorities title VISIBLE
        and the spinner associated with checkbox c GONE. */
        if (numOfCheckBoxesChecked > 1) {
            prioritiesTextView.visibility = View.VISIBLE
            checkBoxesAndSpinners[c]?.visibility = View.GONE
            spinnersVisible[checkBoxesAndSpinners[c]!!] = false

            // For all other checkboxes in the screen, if they are checked, make their spinners visible.
            setOtherSpinnersVisible(c)
        } else {
            prioritiesTextView.visibility = View.GONE
            checkBoxesAndSpinners[c]?.visibility = View.GONE

            // Any other VISIBLE spinners will be set to GONE.
            setVisibleSpinnersToGone()
        }
    }

    /**
     * Handles click events for checkboxes and the savePreferencesButton.
     */
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick")

        when (v) {
            generallyHealthyCheckBox -> {
                if (generallyHealthyCheckBox.isChecked) {
                    handleCheckedCheckBox(generallyHealthyCheckBox)
                } else if (!generallyHealthyCheckBox.isChecked) {
                    handleUncheckedCheckBox(generallyHealthyCheckBox)
                }
            }

            lowCalorieCheckBox -> {
                if (lowCalorieCheckBox.isChecked) {
                    handleCheckedCheckBox(lowCalorieCheckBox)
                } else if (!lowCalorieCheckBox.isChecked) {
                    handleUncheckedCheckBox(lowCalorieCheckBox)
                }
            }

            lowCarbCheckBox -> {
                if (lowCarbCheckBox.isChecked) {
                    handleCheckedCheckBox(lowCarbCheckBox)
                } else if (!lowCarbCheckBox.isChecked) {
                    handleUncheckedCheckBox(lowCarbCheckBox)
                }
            }

            highProteinCheckBox -> {
                if (highProteinCheckBox.isChecked) {
                    handleCheckedCheckBox(highProteinCheckBox)
                } else if (!highProteinCheckBox.isChecked) {
                    handleUncheckedCheckBox(highProteinCheckBox)
                }
            }

            // If the savePreferencesButton is clicked, check if at least one recommendation preference (checkbox) was selected.
            savePreferencesButton -> {
                if (numOfCheckBoxesChecked == 0) {
                    Toast.makeText(
                        activity,
                        "Please select at least one recommendation preference",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    /* This HashMap will store the selected preferences and the selected priorities
                    (for multiple preferences). If only one preference is selected, its priority is 1. */
                    val recommendationPreferencesAndPriorities: HashMap<String, Int> = HashMap()

                    /* If only one checkbox was checked, get its text. Store it in
                    recommendationPreferencesAndPriorities alongside a priority of 1. */
                    if (numOfCheckBoxesChecked == 1) {
                        for (checkBox in checkBoxesChecked) {
                            if (checkBox.value) {
                                val recommendationPreference =
                                    (checkBox.key as CheckBox).text.toString()
                                recommendationPreferencesAndPriorities[recommendationPreference] = 1

                                Log.d(TAG, "recommendationPreference: $recommendationPreference")

                                Log.d(
                                    TAG,
                                    "recommendationPreferencesAndPriorities: $recommendationPreferencesAndPriorities"
                                )

                                // Save the selected preference in the database.
                                savePreferencesInDb(recommendationPreferencesAndPriorities)
                            }
                        }
                    }
                    // If more than one checkbox was checked, find out if priorities were assigned to them using the spinners.
                    else {
                        // For every checked box, get its string value.
                        for (checkBox in checkBoxesChecked) {
                            if (checkBox.value) {
                                val recommendationPreference =
                                    (checkBox.key as CheckBox).text.toString()

                                Log.d(TAG, "recommendationPreference: $recommendationPreference")

                                // Get the priority value selected in the checkbox's corresponding spinner.
                                val selectedPriority =
                                    (checkBoxesAndSpinners[checkBox.key] as Spinner).selectedItem.toString()

                                // Store the recommendation preference and its priority in recommendationPreferencesAndPriorities.
                                recommendationPreferencesAndPriorities[recommendationPreference] =
                                    selectedPriority.toInt()

                                Log.d(TAG, "selectedPriority.toInt(): " + selectedPriority.toInt())
                            }
                        }

                        // Check if the selected priority values are distinct.
                        var allDistinct = true
                        val priorityValues = recommendationPreferencesAndPriorities.values.sorted()
                        for (i in 0 until (priorityValues.size - 1)) {
                            if (priorityValues[i] == priorityValues[i + 1]) {
                                allDistinct = false
                                break
                            }
                        }
                        // If not all priorities are distinct, tell the user.
                        if (!allDistinct) {
                            Toast.makeText(
                                activity,
                                "Please select distinct priority values",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        /* Otherwise, create a bundle of the selected recommendation preferences and
                        priorities and send it to the next fragment. */
                        else {

                            Log.d(
                                TAG,
                                "recommendationPreferencesAndPriorities: $recommendationPreferencesAndPriorities"
                            )

                            // Save the selected preferences and priorities in the database.
                            savePreferencesInDb(recommendationPreferencesAndPriorities)
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the user's saved preferences in the database. If the user was required to select
     * preferences before being able to receive dish recommendations, then redirect them to InitiateScanningDishNamesFragment.
     */
    private fun savePreferencesInDb(recommendationPreferencesAndPriorities: HashMap<String, Int>) {

        Log.d(TAG, "savePreferencesInDb")

        val preferencesAndPriorities: ArrayList<SavedRecommendationPreference> = ArrayList()

        // Copy the preferences and priorities from recommendationPreferencesAndPriorities to preferencesAndPriorities.
        for (entry in recommendationPreferencesAndPriorities) {

            // The preferenceId will be auto-generated by SQLite, but 0 is provided here to be able to create the object.
            val savedRecommendationPreference =
                SavedRecommendationPreference(0, entry.key, entry.value)
            preferencesAndPriorities.add(savedRecommendationPreference)
        }

        Log.d(TAG, "preferencesAndPriorities: $preferencesAndPriorities")

        // Remove all currently stored preferences and priorities from the database.
        viewModel.deleteAllRecommendationPreferences()

        // Add the newly selected preferences and priorities to the database.
        viewModel.insertAllRecommendationPreferences(preferencesAndPriorities)

        Toast.makeText(activity, "Your recommendation preferences are saved", Toast.LENGTH_SHORT)
            .show()

        /* If the user had no saved preferences yet, then they are a first time user, and had to
        select preferences before being able to receive dish recommendations. Since preferences
        were selected and are saved now, redirect them to InitiateScanningDishNamesFragment. */
        if (noSavedPreferencesYet) {
            navController.navigate(
                R.id.action_preferencesFragment_to_initiateScanningDishNamesFragment
            )
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "onItemSelected")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d(TAG, "onNothingSelected")
    }
}