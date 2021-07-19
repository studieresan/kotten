package com.studieresan.studs.happenings

import HappeningCreateMutation
import HappeningsQuery
import UsersQuery
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.studieresan.studs.BuildConfig.MAPS_API_KEY
import com.studieresan.studs.R
import com.studieresan.studs.StudsApplication
import com.studieresan.studs.data.StudsPreferences
import com.studieresan.studs.graphql.apolloClient
import com.studieresan.studs.happenings.viewmodels.HappeningsViewModel
import com.studieresan.studs.net.StudsRepository
import kotlinx.android.synthetic.main.fragment_happenings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import type.GeoJSONFeatureInputType
import type.GeometryInputType
import type.HappeningInput
import type.PropertiesInputType
import javax.inject.Inject

class CreateHappeningActivity : AppCompatActivity() {

    private var selectedEmoji: RadioButton? = null
    private var coordinates: List<Double>? = null
    private var locationName: String? = null
    private var locationType: String? = null
    private var hostID: String? = null

    private var participants = mutableListOf<String>()

    @Inject
    lateinit var studsRepository: StudsRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_happening)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        StudsApplication.applicationComponent.inject(this)
        hostID = StudsPreferences.getID(this)

        // Initialize the SDK
        Places.initialize(applicationContext, MAPS_API_KEY)
        val autocompleteFragment =
                supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                        as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES))

        // Sets location bias to Sweden, change to device location in future
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(LatLng(55.37514, 11.1712), LatLng(67.85572, 23.15645)))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                locationName = place.name
                locationType = place.types?.first().toString()
                if (place.latLng != null) {
                    coordinates = listOf(place.latLng!!.longitude, place.latLng!!.latitude)
                }
                autocompleteFragment.setText(place.name)
            }

            override fun onError(p0: Status) {
                raiseAlert(this@CreateHappeningActivity.getString(R.string.generic_error_message), this@CreateHappeningActivity)
            }
        })

        selectedEmoji = findViewById<View>(R.id.create_emoji1) as RadioButton

        val radioGroup = findViewById<View>(R.id.create_rb) as RadioGroup
        radioGroup.setOnCheckedChangeListener { _, checkedId -> // checkedId is the RadioButton selected
            selectedEmoji!!.alpha = 0.3F
            selectedEmoji = findViewById<View>(checkedId) as RadioButton
            selectedEmoji!!.alpha = 1F
        }

        val addButton = findViewById<View>(R.id.btn_add_happening) as Button
        addButton.setOnClickListener {
            createHappening(this)
        }

        val cancelButton = findViewById<View>(R.id.btn_cancel_happening) as Button
        cancelButton.setOnClickListener {
            finish()
        }

        loadUsers(this)
    }

    private fun loadUsers(context: Context) {

        val states = arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
        )

        val colors = intArrayOf(ContextCompat.getColor(context, R.color.lightGrey), ContextCompat.getColor(context, R.color.colorPrimaryLight))
        val colorsStateList = ColorStateList(states, colors)

        CoroutineScope(Dispatchers.Main).launch {

            val response = try {
                apolloClient(context).query(UsersQuery()).await()
            } catch (e: ApolloException) {
                null
            }

            var users = response?.data?.users?.filterNotNull()?.sortedBy { user -> user.firstName }
            val participantChips = findViewById<View>(R.id.create_participant_chips) as ChipGroup
            users?.forEach { user ->

                if (user.id != hostID) {
                    val chip = Chip(context).apply {
                        text = "${user.firstName} ${user.lastName?.get(0)}"
                        isCloseIconVisible = false
                        isClickable = true
                        isCheckable = true
                        checkedIcon = null
                        alpha = 0.75F
                        chipBackgroundColor = colorsStateList
                        setOnCheckedChangeListener { chip, isChecked ->
                            if (isChecked && user.id != null) {
                                participants.add(user.id)
                                chip.alpha = 1F
                            } else {
                                participants.remove(user.id)
                                chip.alpha = 0.85F
                            }
                        }
                    }

                    Glide.with(context)
                            .asBitmap()
                            .load(user.info?.picture)
                            .apply(RequestOptions.circleCropTransform())
                            .error(R.drawable.ic_person_black_24dp)
                            .into(object : CustomTarget<Bitmap?>() {
                                override fun onResourceReady(resource: Bitmap, @Nullable transition: Transition<in Bitmap?>?) {
                                    val drawable = BitmapDrawable(resource)
                                    chip.chipIcon = drawable
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })

                    participantChips.addView(chip)
                }

            }

        }

    }

    private fun createHappening(context: Context) {

        if (coordinates.isNullOrEmpty()) {
            raiseAlert(getString(R.string.happenings_no_location), this)
            return
        }

        val progressBar = findViewById<View>(R.id.create_progress_bar)
        progressBar.isVisible = true

        val description = findViewById<View>(R.id.create_desc) as TextInputLayout
        var title = if (participants.isEmpty()) "Sitter ensam, joina!" else "Sitter med ${participants.size} andra!"

        val happening = HappeningInput(
                host = hostID.toInput(),
                participants = participants.toInput(),
                location = GeoJSONFeatureInputType(type = "Feature".toInput(),
                        geometry = GeometryInputType(
                                type = locationType.toInput(),
                                coordinates = coordinates.toInput()).toInput(),
                        properties = PropertiesInputType(locationName.toInput()).toInput()).toInput(),
                title = title.toInput(),
                emoji = selectedEmoji?.text.toString().toInput(),
                description = description.editText?.text.toString().toInput())

        CoroutineScope(Dispatchers.Main).launch {
            try {
                apolloClient(context).mutate(HappeningCreateMutation(happening = happening.toInput())).await()
                progressBar.isVisible = false
                fetchHappenings()
                finish()
            } catch (e: ApolloException) {
                raiseAlert(getString(R.string.generic_error_message), this@CreateHappeningActivity)
            }
        }
    }

    private fun fetchHappenings() {
        CoroutineScope(Dispatchers.Main).launch {
            val response = try {
                apolloClient(this@CreateHappeningActivity).query(HappeningsQuery()).await()
            } catch (e: ApolloException) {
                null
            }

            val happenings = response?.data?.happenings?.filterNotNull()?.sortedByDescending { happening -> happening.created }
            val viewModel = ViewModelProviders.of(this@CreateHappeningActivity).get(HappeningsViewModel::class.java)
            viewModel.setHappenings(happenings)

        }
    }

    private fun raiseAlert(text: String, context: Context) {
        MaterialAlertDialogBuilder(context)
                .setTitle(text)
                .setPositiveButton(getString(R.string.confirm), null)
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Places.deinitialize()
    }

}
