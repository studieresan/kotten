package com.studieresan.studs.happenings

import HappeningsQuery
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.android.gms.maps.model.LatLng
import com.studieresan.studs.R
import com.studieresan.studs.StudsApplication
import com.studieresan.studs.graphql.apolloClient
import com.studieresan.studs.happenings.adapters.HappeningsPagerAdapter
import com.studieresan.studs.happenings.viewmodels.HappeningsViewModel
import com.studieresan.studs.net.StudsRepository
import kotlinx.android.synthetic.main.fragment_happenings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class HappeningsFragment : Fragment() {

    private lateinit var adapter: HappeningsPagerAdapter
    private var viewModel: HappeningsViewModel? = null

    @Inject
    lateinit var studsRepository: StudsRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_happenings, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        StudsApplication.applicationComponent.inject(this)

        viewModel = ViewModelProviders.of(requireActivity()).get(HappeningsViewModel::class.java)

        viewModel!!.mapCenter.observe(viewLifecycleOwner, Observer<LatLng> {
            selectTab(0)
        })

        btn_create_happening.setOnClickListener {
            val intent = Intent(context, CreateHappeningActivity::class.java)
            startActivity(intent)
        }

        fetchHappenings()
        happenings_swipe_refresh.setOnRefreshListener { fetchHappenings() }

        happenings_view_pager.offscreenPageLimit = 2

        adapter = HappeningsPagerAdapter(this, parentFragmentManager)
        happenings_view_pager.adapter = adapter
        happenings_tabs.setupWithViewPager(happenings_view_pager)

    }

    private fun fetchHappenings() {

        CoroutineScope(Dispatchers.Main).launch {
            val response = try {
                apolloClient(requireContext()).query(HappeningsQuery()).await()
            } catch (e: ApolloException) {
                null
            }
            happenings_swipe_refresh.isRefreshing = false
            happenings_progress_bar.visibility = View.GONE

            val happenings = response?.data?.happenings?.filterNotNull()?.sortedByDescending { happening -> happening.created }
            viewModel?.setHappenings(happenings)

        }
    }

    fun selectTab(position: Int) {
        happenings_view_pager.currentItem = position
    }

}
