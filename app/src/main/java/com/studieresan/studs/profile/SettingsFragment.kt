package com.studieresan.studs.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.studieresan.studs.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val darkModeSwitch = findPreference<SwitchPreference>("darkmode_preference")

        darkModeSwitch?.setOnPreferenceChangeListener { _, isChecked ->
            if (isChecked as Boolean) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                darkModeSwitch.isChecked = true
                true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                darkModeSwitch.isChecked = false
                false
            }
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
