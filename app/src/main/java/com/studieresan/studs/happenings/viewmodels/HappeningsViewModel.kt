package com.studieresan.studs.happenings.viewmodels

import HappeningsQuery
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class HappeningsViewModel : ViewModel() {

    val happenings = MutableLiveData<List<HappeningsQuery.Happening>>()
    val mapCenter = MutableLiveData(LatLng(59.334591, 18.063240))

    fun setHappenings(h: List<HappeningsQuery.Happening>?) {
        happenings.value = h
    }

    fun setMapCenter(location: LatLng) {
        mapCenter.value = location
    }
}
