package com.example.repeatingalarmfoss.screens.added_tasks

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.repeatingalarmfoss.screens.settings.SettingsFragment

class MainScreenViewPagerAdapter(private val activity: MainActivity, supportFragmentManager: FragmentManager) : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> TaskListFragment.newInstance(activity)
        1 -> SettingsFragment()
        else -> throw IllegalStateException()
    }
}
