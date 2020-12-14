package se.studieresan.studs.data

import android.content.Context
import android.preference.PreferenceManager
import se.studieresan.studs.R

private const val NAME = "name"
private const val PICTURE = "picture"
private const val POSITION = "position"
private const val LOGGED_IN = "logged_in"
private const val EMAIL = "email"
private const val PERMISSIONS = "permissions"
private const val JWT_TOKEN = "jwt_token"

// Abstraction over SharedPrefs
object StudsPreferences {

    fun isLoggedIn(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(LOGGED_IN, false)

    fun logIn(context: Context, email: String) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(LOGGED_IN, true)
            .putString(EMAIL, email)
            .apply()

    fun getEmail(context: Context) =
        checkNotNull(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(EMAIL, "")
        )

    fun setName(context: Context, name: String) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(NAME, name)
            .apply()

    fun getName(context: Context) = checkNotNull(
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(NAME, "")
    )

    fun setPicture(context: Context, url: String) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PICTURE, url)
            .apply()

    fun getPicture(context: Context) = checkNotNull(
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            PICTURE, ""
        )
    )

    fun setPosition(context: Context, position: String?) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(POSITION, if (position != null) position else "")
            .apply()

    fun getPosition(context: Context) = checkNotNull(
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            POSITION, ""
        )
    )

    fun logOut(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(LOGGED_IN, false)
            .putString(JWT_TOKEN, "")
            .putString(NAME, "")
            .putString(PICTURE, "")
            .putString(POSITION, "")
            .apply()
    }

    fun setPermissions(context: Context, permissions: List<String>?) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putStringSet(PERMISSIONS, if (permissions != null) permissions.toSet() else listOf<String>().toSet())
            .apply()

    fun getPermissions(context: Context): Set<String> = checkNotNull(
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(
            PERMISSIONS, emptySet()
        )
    )

    fun liveLocationSharingIsAllowed(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.live_location_preference_key),
            true
        )

    fun setJwtToken(context: Context, token: String) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(JWT_TOKEN, token)
            .apply()

    fun getJwtToken(context: Context) =
        checkNotNull(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(JWT_TOKEN, "")
        )

    fun getDarkModePreferences(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.darkmode_preference),
            false
    )
}
