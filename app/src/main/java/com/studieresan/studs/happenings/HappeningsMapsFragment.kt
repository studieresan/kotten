package com.studieresan.studs.happenings

import HappeningsQuery
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.studieresan.studs.R
import com.studieresan.studs.happenings.viewmodels.HappeningsViewModel
import java.time.OffsetDateTime

class HappeningsMapsFragment : Fragment() {

    private var loaded: Boolean = false
    private var centerLocation: LatLng? = null
    private var map: GoogleMap? = null

    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        val viewModel = ViewModelProviders.of(requireActivity()).get(HappeningsViewModel::class.java)

        viewModel.mapCenter.observe(viewLifecycleOwner, Observer { center ->
            centerLocation = center
            val cameraPosition = CameraPosition.Builder()
                    .target(center)
                    .zoom(16f)
                    .build()
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        })

        viewModel.happenings.observe(viewLifecycleOwner, Observer { happenings ->
            happenings.map { happening ->
                if (!happening.location?.geometry?.coordinates.isNullOrEmpty() && happening.location?.geometry?.coordinates?.get(0) != null && happening.location.geometry.coordinates[1] != null) {

                    val coordinates = LatLng(happening.location.geometry.coordinates[1]!!.toDouble(), happening.location.geometry.coordinates[0]!!.toDouble())
                    val odt = OffsetDateTime.now()
                    val zoneOffset = odt.offset
                    val dateInMilli = happening.created?.atOffset(zoneOffset)?.toInstant()?.toEpochMilli()
                    val displayDate = if (dateInMilli != null) DateUtils.formatDateTime(context, dateInMilli, DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_TIME or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY).capitalize() else ""

                    val marker = MarkerOptions().position(coordinates).title("${happening.host?.firstName} ${happening.host?.lastName?.get(0)} ${happening.title?.decapitalize()} ${happening.emoji}")
                            .snippet("$displayDate @ ${happening.location.properties?.name}")

                    Glide.with(this)
                            .asBitmap()
                            .load(happening.host?.info?.picture)
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions().override(100, 100))
                            .error(R.drawable.ic_person_black_24dp)
                            .into(object : CustomTarget<Bitmap?>() {
                                override fun onResourceReady(resource: Bitmap, @Nullable transition: Transition<in Bitmap?>?) {
                                    marker.icon(BitmapDescriptorFactory.fromBitmap(resource))
                                    googleMap
                                            .addMarker(marker)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })

                    if (!loaded) {
                        val cameraPosition = CameraPosition.Builder()
                                .target(coordinates)
                                .zoom(12f)
                                .build()
                        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        loaded = true
                    }
                }
            }

        })
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
