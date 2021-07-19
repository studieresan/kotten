package com.studieresan.studs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.studieresan.studs.events.views.EventsFragment
import com.studieresan.studs.happenings.HappeningsFragment
import com.studieresan.studs.profile.ProfileFragment
import com.studieresan.studs.util.inTransaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : StudsActivity() {

    private var currentFragmentId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addToolbar()
        bottom_navigation.setOnNavigationItemSelectedListener(navItemSelectListener)

        // If we don't have a current Fragment from the bundle, jump to Events
        if (savedInstanceState == null) {
            replaceFragment(R.id.navigation_events, EventsFragment())
            setToolbarTitle(R.string.event)
        }

    }

    private val navItemSelectListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (currentFragmentId == item.itemId) {
            return@OnNavigationItemSelectedListener true
        }

        when (item.itemId) {
            R.id.navigation_events -> {
                setToolbarTitle(R.string.event)
                replaceFragment(R.id.navigation_events, EventsFragment())
            }
            R.id.navigation_happenings -> {
                setToolbarTitle(R.string.happenings)
                replaceFragment(R.id.navigation_happenings, HappeningsFragment())
            }
            R.id.navigation_profile -> {
                setToolbarTitle(R.string.profile)
                replaceFragment(R.id.navigation_profile, ProfileFragment())
            }
        }

        return@OnNavigationItemSelectedListener true
    }

    private fun <F> replaceFragment(id: Int, fragment: F) where F : Fragment {
        currentFragmentId = id

        supportFragmentManager.inTransaction {
            replace(FRAGMENT_ID, fragment)
        }
    }

    companion object {
        fun makeIntent(context: Context, newTask: Boolean = false): Intent {
            val intent = Intent(context, MainActivity::class.java)
            if (newTask) {
                intent.run {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
            return intent
        }

        private const val FRAGMENT_ID = R.id.fragment_container
    }

}
