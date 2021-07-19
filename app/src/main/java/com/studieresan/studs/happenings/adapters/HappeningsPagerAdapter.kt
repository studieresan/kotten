package com.studieresan.studs.happenings.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.studieresan.studs.R
import com.studieresan.studs.happenings.HappeningsFragment
import com.studieresan.studs.happenings.HappeningsListFragment
import com.studieresan.studs.happenings.HappeningsMapsFragment


class HappeningsPagerAdapter(private val context: HappeningsFragment, fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return if (position === 0) HappeningsMapsFragment() else HappeningsListFragment()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position === 0) context.getString(R.string.map_tab) else context.getString(R.string.list_tab)
    }

    override fun getCount(): Int {
        return 2
    }

}
