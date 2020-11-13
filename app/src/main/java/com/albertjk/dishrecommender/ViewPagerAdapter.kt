package com.albertjk.dishrecommender

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * Loads the tutorial images in the ViewPager.
 */
class ViewPagerAdapter(private val context: Context) : PagerAdapter() {

    private lateinit var layoutInflater: LayoutInflater

    // This array stores references to the drawable images to display for the tutorial.
    private val tutorialImages = arrayOf(R.drawable.tutorial_image_1, R.drawable.tutorial_image_2, R.drawable.tutorial_image_3, R.drawable.tutorial_image_4, R.drawable.tutorial_image_5)

    /**
     * Returns the number of available views (tutorial images).
     */
    override fun getCount(): Int = tutorialImages.size

    /**
     * Determines if a page view is associated with a specific key object as returned by instantiateItem().
     */
    override fun isViewFromObject(view: View, `object`: Any): Boolean = (view == `object`)

    /**
     * Creates the page, which shows a tutorial image, for the given position.
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.view_pager_image, null)

        val imageViewInViewPager = view.findViewById<ImageView>(R.id.imageViewInViewPager)

        // Set the current image to be displayed in the imageViewInViewPager view.
        imageViewInViewPager.setImageResource(tutorialImages[position])

        val viewPager: ViewPager = container as ViewPager
        viewPager.addView(view, 0)
        return view
    }

    /**
     * Removes a page, which shows a tutorial image, for the given position.
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager: ViewPager = container as ViewPager
        val view: View = `object` as View
        viewPager.removeView(view)
    }
}