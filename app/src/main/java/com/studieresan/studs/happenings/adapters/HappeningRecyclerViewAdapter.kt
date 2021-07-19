package com.studieresan.studs.happenings.adapters

import HappeningDeleteMutation
import HappeningsQuery
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.studieresan.studs.R
import com.studieresan.studs.data.StudsPreferences
import com.studieresan.studs.graphql.apolloClient
import com.studieresan.studs.happenings.HappeningsListFragment
import com.studieresan.studs.happenings.viewmodels.HappeningsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class HappeningRecyclerViewAdapter(
        private val values: List<HappeningsQuery.Happening>,
        private val parentFragment: HappeningsListFragment)
    : RecyclerView.Adapter<HappeningRecyclerViewAdapter.ViewHolder>() {

    private var context: Context? = null
    private var viewModel: HappeningsViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_happening, parent, false)

        viewModel = ViewModelProviders.of(parentFragment.requireActivity()).get(HappeningsViewModel::class.java)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val happenings = values
        val happening = happenings[position]
        val odt = OffsetDateTime.now()
        val zoneOffset = odt.offset
        val dateInMilli = happening.created?.atOffset(zoneOffset)?.toInstant()?.toEpochMilli()
        val displayDate = if (dateInMilli != null) DateUtils.formatDateTime(context, dateInMilli, DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_TIME or DateUtils.FORMAT_SHOW_WEEKDAY).capitalize() else ""
        val detailedDate = if (dateInMilli != null) DateUtils.formatDateTime(context, dateInMilli, DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY).capitalize() else ""

        holder.emojiView.text = happening.emoji
        holder.titleView.text = happening.title
        holder.descView.text = happening.description
        holder.timeView.text = displayDate
        holder.placeView.text = "@ ${happening.location?.properties?.name}"

        holder.cardView.setOnClickListener {

            if (!happening.location?.geometry?.coordinates.isNullOrEmpty() && happening.location?.geometry?.coordinates?.get(0) != null && happening.location.geometry.coordinates.get(1) != null) {
                val coordinates = LatLng(happening.location.geometry.coordinates[1]!!.toDouble(), happening.location.geometry.coordinates[0]!!.toDouble())
                viewModel?.setMapCenter(coordinates)
            }
        }

        holder.cardView.setOnLongClickListener {

            val customView: View = LayoutInflater.from(context).inflate(R.layout.dialog_happening, null)
            customView.findViewById<TextView>(R.id.info_window_title).text = happening.title
            customView.findViewById<TextView>(R.id.info_window_emoji).text = happening.emoji
            customView.findViewById<TextView>(R.id.info_window_desc).text = happening.description
            customView.findViewById<TextView>(R.id.info_window_date).text = detailedDate

            Glide.with(it)
                    .load(happening.host?.info?.picture)
                    .apply(RequestOptions.circleCropTransform())
                    .into(customView.findViewById(R.id.info_window_host))

            if (!happening.participants.isNullOrEmpty()) {
                setParticipants(customView, happening.participants)
            }

            val builder = MaterialAlertDialogBuilder(context!!)
                    .setView(customView)
                    .setPositiveButton(context!!.getString(R.string.close)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

            builder.show()
            true
        }

        if (context != null && happening.host?.id == StudsPreferences.getID(context!!)) {
            holder.deleteBtn.isVisible = true
            holder.deleteBtn.setOnClickListener {
                MaterialAlertDialogBuilder(context!!)
                        .setTitle(context!!.getString(R.string.happenings_delete))
                        .setNegativeButton(context!!.getString(R.string.confirm)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(context!!.getString(R.string.happenings_delete_confirm)) { dialog, which ->
                            CoroutineScope(Dispatchers.Main).launch {
                                val response = try {
                                    apolloClient(context!!).mutate(HappeningDeleteMutation(id = happening.id.toInput())).await()
                                    notifyItemRemoved(position)
                                    dialog.dismiss()
                                } catch (e: ApolloException) {
                                    null
                                }
                            }
                        }
                        .create()
                        .show()
                true
            }
        }

        // Set the image
        context?.let {

            Glide.with(it)
                    .load(happening.host?.info?.picture)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descView: TextView = view.findViewById((R.id.happening_desc))
        val emojiView: TextView = view.findViewById(R.id.happening_emoji)
        val imageView: ImageView = view.findViewById((R.id.happening_host))
        val placeView: TextView = view.findViewById(R.id.happening_place)
        val timeView: TextView = view.findViewById(R.id.happening_time)
        val titleView: TextView = view.findViewById(R.id.happening_title)
        val deleteBtn: Button = view.findViewById(R.id.happening_delete)
        val cardView: LinearLayout = view.findViewById(R.id.happening_card)

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

    private fun setParticipants(view: View, participants: List<HappeningsQuery.Participant?>?) {
        val participantChips = view.findViewById<View>(R.id.info_window_participants) as ChipGroup
        participants?.forEach { user ->
            if (user != null) {
                if (user.id != context?.let { StudsPreferences.getID(it) }) {
                    val chip = Chip(context).apply {
                        text = "${user.firstName} ${user.lastName?.get(0)}"
                        isCloseIconVisible = false
                        checkedIcon = null
                    }

                    participantChips.addView(chip)
                }
            }
        }
    }
}
