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

class GeneratingRecommendationsFragment : Fragment() {

    private val TAG = GeneratingRecommendationsFragment::class.qualifiedName

    private lateinit var navController: NavController

    // This list will store the user's saved recommendation preferences and priorities retrieved from the database.
    private var preferences: List<SavedRecommendationPreference>? = null

    private var identifiedDishNames: ArrayList<String>? = null

    private lateinit var viewModel: ViewModel

    // This list will store all the dishes retrieved from the database.
    private lateinit var dishList: List<Dish>

    // This list will store all the data associated with the scanned and identified dish names.
    private lateinit var identifiedDishes: ArrayList<Dish>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        identifiedDishNames = ArrayList()

        // Get the names of the identified dishes.
        arguments?.getStringArrayList("identifiedDishNames")?.let {
            identifiedDishNames!!.addAll(
                it
            )
        }

        Log.d(TAG, "received identifiedDishNames: " + identifiedDishNames.toString())

        identifiedDishes = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generating_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the navController which has a reference to the navigation graph.
        navController = Navigation.findNavController(view)

        // Assign the ViewModel.
        viewModel = activity?.let { ViewModelProvider(it).get(ViewModel::class.java) }!!

        // Get all dish data from the database.
        dishList = viewModel.allDishes.value!!

        Log.d(TAG, "dishList: $dishList")

        // Get the user's saved dish recommendation preferences and priorities from the database.
        preferences = viewModel.savedPreferences.value!!

        Log.d(TAG, "preferences: $preferences")
    }

    override fun onStart() {
        super.onStart()

        getNutritionData()
        produceRecommendations()
    }

    /**
     * Retrieves the nutrition data of the identified dishes from the database.
     */
    private fun getNutritionData() {
        // Get all the data associated with each scanned and identified dish name.
        for (dishName in identifiedDishNames!!) {
            for (dish in dishList) {
                if (dish.dishName.toLowerCase() == dishName.toLowerCase()) {
                    identifiedDishes.add(dish)
                }
            }
        }
        Log.d(TAG, "identifiedDishes: $identifiedDishes")

        Log.d(TAG, "Got nutrition data")
    }

    /**
     * Sorts the identifiedDishes list into order based on the selected recommendation preferences,
     * and then redirects to OurRecommendationsFragment.
     */
    private fun produceRecommendations() {

        Log.d(TAG, "Producing recommendations...")

        /* Calculate the healthiness score of each scanned and identified dish,
        in case a selected preference is generally healthy. */
        for (i in 0 until identifiedDishes.size) {
            identifiedDishes[i].healthinessScore = calculateHealthinessScore(identifiedDishes[i])
        }

        // This Hash Map will map object properties to their priorities, which were set by the user.
        var dishPropertiesAndPriorities: HashMap<String, Int> = HashMap()

        /* First copy over the names and priorities from the preferences list,
        but, replace the string sentences of the preference names with object property names. */
        for (pref in preferences!!) {
            when (pref.preferenceName) {
                RecommendationPreferenceNameConstants.GENERALLY_HEALTHY -> {
                    dishPropertiesAndPriorities["healthinessScore"] = pref.selectedPriority
                }
                RecommendationPreferenceNameConstants.LOW_CALORIES -> {
                    dishPropertiesAndPriorities["calories"] = pref.selectedPriority
                }
                RecommendationPreferenceNameConstants.LOW_CARB -> {
                    dishPropertiesAndPriorities["carbohydrates"] = pref.selectedPriority
                }
                RecommendationPreferenceNameConstants.HIGH_PROTEIN -> {
                    dishPropertiesAndPriorities["protein"] = pref.selectedPriority
                }
            }
        }

        // If there are multiple entries in dishPropertiesAndPriorities, sort them by value (priority).
        if (dishPropertiesAndPriorities.size > 1) {
            dishPropertiesAndPriorities =
                dishPropertiesAndPriorities.toList().sortedBy { (_, value) -> value }
                    .toMap() as HashMap<String, Int>
        }

        Log.d(
            TAG,
            "Sorted dishPropertiesAndPriorities: $dishPropertiesAndPriorities"
        )

        // These constants represent the two possible sorting orders - ascending and descending.
        val ASCENDING = "ascending"
        val DESCENDING = "descending"

        // This HashMap will store the sorting order of object properties (ascending or descending).
        val dishPropertiesAndSortingOrders: HashMap<String, String> = HashMap()
        dishPropertiesAndSortingOrders["healthinessScore"] = DESCENDING
        dishPropertiesAndSortingOrders["calories"] = ASCENDING
        dishPropertiesAndSortingOrders["carbohydrates"] = ASCENDING
        dishPropertiesAndSortingOrders["protein"] = DESCENDING

        // Check how many recommendation preferences were selected. Also check the priorities selected for them.

        /* If only one recommendation preference was selected, find out which one.
        Its priority is always 1, so the priority is not important here. */
        if (dishPropertiesAndPriorities.size == 1) {
            Log.d(TAG, "One recommendation preferences")

            // Get the object property representing the selected preference.
            val preferenceProperty = dishPropertiesAndPriorities.keys.elementAt(0)

            // Get whether the dish list should be sorted in ascending or descending order based on the object property.
            val order = dishPropertiesAndSortingOrders[preferenceProperty]

            // Sort the identifiedDishes list based on the object property and the associated sorting order.
            if (order == DESCENDING) {
                identifiedDishes.sortWith(compareByDescending { it.getProperty(preferenceProperty) })
            } else if (order == ASCENDING) {
                identifiedDishes.sortWith(compareBy { it.getProperty(preferenceProperty) })
            }
        }
        /* If two recommendation preferences were selected, sort the identifiedDishes list by the
        preference with priority 1, then by the preference with priority 2. */
        else if (dishPropertiesAndPriorities.size == 2) {
            Log.d(TAG, "Two recommendation preferences")

            // Get the object properties representing the selected preferences.
            val preferenceProperty1 = dishPropertiesAndPriorities.keys.elementAt(0)
            val preferenceProperty2 = dishPropertiesAndPriorities.keys.elementAt(1)

            // Get whether the dish list should be sorted in ascending or descending order based on the object properties.
            val order1 = dishPropertiesAndSortingOrders[preferenceProperty1]
            val order2 = dishPropertiesAndSortingOrders[preferenceProperty2]

            Log.d(TAG, "dishPropertiesAndPriorities: $dishPropertiesAndPriorities")

            Log.d(TAG, "preferenceProperty1: $preferenceProperty1")
            Log.d(TAG, "preferenceProperty2: $preferenceProperty2")
            Log.d(TAG, "order1: $order1")
            Log.d(TAG, "order2: $order2")

            // Sort the identifiedDishes list based on the object properties and their associated sorting orders.
            if (order1 == DESCENDING && order2 == DESCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenByDescending { it.getProperty(preferenceProperty2) })
            } else if (order1 == ASCENDING && order2 == ASCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenBy {
                    it.getProperty(
                        preferenceProperty2
                    )
                })
            } else if (order1 == DESCENDING && order2 == ASCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenBy { it.getProperty(preferenceProperty2) })
            } else if (order1 == ASCENDING && order2 == DESCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenByDescending {
                    it.getProperty(
                        preferenceProperty2
                    )
                })
            }
        }
        /* If three recommendation preferences were selected, sort the identifiedDishes
        list by the preference with priority 1, then by the preference with priority 2,
        and finally by the preference with priority 3. */
        else if (dishPropertiesAndPriorities.size == 3) {
            Log.d(TAG, "Three recommendation preferences")

            // Get the object properties representing the selected preferences.
            val preferenceProperty1 = dishPropertiesAndPriorities.keys.elementAt(0)
            val preferenceProperty2 = dishPropertiesAndPriorities.keys.elementAt(1)
            val preferenceProperty3 = dishPropertiesAndPriorities.keys.elementAt(2)

            // Get whether the dish list should be sorted in ascending or descending order based on the object properties.
            val order1 = dishPropertiesAndSortingOrders[preferenceProperty1]
            val order2 = dishPropertiesAndSortingOrders[preferenceProperty2]
            val order3 = dishPropertiesAndSortingOrders[preferenceProperty3]

            Log.d(TAG, "dishPropertiesAndPriorities: $dishPropertiesAndPriorities")

            Log.d(TAG, "preferenceProperty1: $preferenceProperty1")
            Log.d(TAG, "preferenceProperty2: $preferenceProperty2")
            Log.d(TAG, "preferenceProperty3: $preferenceProperty3")
            Log.d(TAG, "order1: $order1")
            Log.d(TAG, "order2: $order2")
            Log.d(TAG, "order3: $order3")

            // Sort the identifiedDishes list based on the object properties and their associated sorting orders.
            if (order1 == DESCENDING && order2 == DESCENDING && order3 == ASCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenByDescending { it.getProperty(preferenceProperty2) }
                    .thenBy { it.getProperty(preferenceProperty3) })
            } else if (order1 == DESCENDING && order2 == ASCENDING && order3 == DESCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenBy { it.getProperty(preferenceProperty2) }
                    .thenByDescending { it.getProperty(preferenceProperty3) })
            } else if (order1 == ASCENDING && order2 == DESCENDING && order3 == DESCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenByDescending {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenByDescending { it.getProperty(preferenceProperty3) })
            } else if (order1 == DESCENDING && order2 == ASCENDING && order3 == ASCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenBy { it.getProperty(preferenceProperty2) }
                    .thenBy { it.getProperty(preferenceProperty3) })
            } else if (order1 == ASCENDING && order2 == DESCENDING && order3 == ASCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenByDescending {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenBy { it.getProperty(preferenceProperty3) })
            } else if (order1 == ASCENDING && order2 == ASCENDING && order3 == DESCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenBy {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenByDescending { it.getProperty(preferenceProperty3) })
            }
        }
        /* If all four recommendation preferences were selected, sort the
        identifiedDishes list by the preference with priority 1,
        then by the preference with priority 2, then by the preference with priority 3,
        and finally by the preference with priority 4. */
        else if (dishPropertiesAndPriorities.size == 4) {

            // Get the object properties representing the selected preferences.
            val preferenceProperty1 = dishPropertiesAndPriorities.keys.elementAt(0)
            val preferenceProperty2 = dishPropertiesAndPriorities.keys.elementAt(1)
            val preferenceProperty3 = dishPropertiesAndPriorities.keys.elementAt(2)
            val preferenceProperty4 = dishPropertiesAndPriorities.keys.elementAt(3)

            // Get whether the dish list should be sorted in ascending or descending order based on the object properties.
            val order1 = dishPropertiesAndSortingOrders[preferenceProperty1]
            val order2 = dishPropertiesAndSortingOrders[preferenceProperty2]
            val order3 = dishPropertiesAndSortingOrders[preferenceProperty3]
            val order4 = dishPropertiesAndSortingOrders[preferenceProperty4]

            Log.d(TAG, "dishPropertiesAndPriorities: $dishPropertiesAndPriorities")

            Log.d(TAG, "preferenceProperty1: $preferenceProperty1")
            Log.d(TAG, "preferenceProperty2: $preferenceProperty2")
            Log.d(TAG, "preferenceProperty3: $preferenceProperty3")
            Log.d(TAG, "preferenceProperty4: $preferenceProperty4")
            Log.d(TAG, "order1: $order1")
            Log.d(TAG, "order2: $order2")
            Log.d(TAG, "order3: $order3")
            Log.d(TAG, "order4: $order4")

            // Sort the identifiedDishes list based on the object properties and their associated sorting orders.
            if (order1 == DESCENDING && order2 == ASCENDING && order3 == DESCENDING && order4 == ASCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenBy { it.getProperty(preferenceProperty2) }
                    .thenByDescending { it.getProperty(preferenceProperty3) }
                    .thenBy { it.getProperty(preferenceProperty4) })
            } else if (order1 == DESCENDING && order2 == ASCENDING && order3 == ASCENDING && order4 == DESCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenBy { it.getProperty(preferenceProperty2) }
                    .thenBy { it.getProperty(preferenceProperty3) }
                    .thenByDescending { it.getProperty(preferenceProperty4) })
            } else if (order1 == DESCENDING && order2 == DESCENDING && order3 == ASCENDING && order4 == ASCENDING) {
                identifiedDishes.sortWith(compareByDescending<Dish> {
                    it.getProperty(
                        preferenceProperty1
                    )
                }.thenByDescending { it.getProperty(preferenceProperty2) }
                    .thenBy { it.getProperty(preferenceProperty3) }
                    .thenBy { it.getProperty(preferenceProperty4) })
            } else if (order1 == ASCENDING && order2 == DESCENDING && order3 == ASCENDING && order4 == DESCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenByDescending {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenBy { it.getProperty(preferenceProperty3) }
                    .thenByDescending { it.getProperty(preferenceProperty4) })
            } else if (order1 == ASCENDING && order2 == DESCENDING && order3 == DESCENDING && order4 == ASCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenByDescending {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenByDescending { it.getProperty(preferenceProperty3) }
                    .thenBy { it.getProperty(preferenceProperty4) })
            } else if (order1 == ASCENDING && order2 == ASCENDING && order3 == DESCENDING && order4 == DESCENDING) {
                identifiedDishes.sortWith(compareBy<Dish> { it.getProperty(preferenceProperty1) }.thenBy {
                    it.getProperty(
                        preferenceProperty2
                    )
                }.thenByDescending { it.getProperty(preferenceProperty3) }
                    .thenByDescending { it.getProperty(preferenceProperty4) })
            }
        }

        // Redirect to OurRecommendationsFragment and pass the sorted dish list in a bundle.
        val bundle = bundleOf(
            "identifiedDishes" to identifiedDishes
        )
        Log.d(TAG, "Sorted identifiedDishes: $identifiedDishes")
        navController.navigate(
            R.id.action_generatingRecommendationsFragment_to_ourRecommendationsFragment,
            bundle
        )
    }

    /**
     * Calculates the healthiness score of a dish based on its total fat, saturated fat,
     * sugar, and salt content and the corresponding daily reference intake values. Returns this score.
     */
    private fun calculateHealthinessScore(dish: Dish): Double =
        1 - ((dish.totalFat / DailyReferenceIntakeConstants.DAILY_REF_TOTAL_FAT_INTAKE +
                dish.saturatedFat / DailyReferenceIntakeConstants.DAILY_REF_SATURATED_FAT_INTAKE +
                dish.sugars / DailyReferenceIntakeConstants.DAILY_REF_SUGARS_INTAKE +
                dish.salt / DailyReferenceIntakeConstants.DAILY_REF_SALT_INTAKE)
                / 4)
}