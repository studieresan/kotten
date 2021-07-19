package com.studieresan.studs.events.views

import EventQuery
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils.*
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.studieresan.studs.R
import com.studieresan.studs.StudsActivity
import com.studieresan.studs.StudsApplication
import com.studieresan.studs.data.IntentExtra
import com.studieresan.studs.events.contracts.EventDetailContract
import com.studieresan.studs.events.presenters.EventDetailPresenter
import com.studieresan.studs.graphql.apolloClient
import kotlinx.android.synthetic.main.activity_event_detail.*
import java.time.OffsetDateTime

class EventDetailActivity : StudsActivity(), EventDetailContract.View {

    private lateinit var eventID: String
    private lateinit var presenter: EventDetailContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StudsApplication.applicationComponent.inject(this)
        setContentView(R.layout.activity_event_detail)
        eventID = intent.getStringExtra(IntentExtra.EVENT_ID)

        val view = this
        var event: EventQuery.Event

        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(view.applicationContext).query(EventQuery(id = eventID.toInput())).await()
            } catch (e: ApolloException) {
                null
            }

            event = response?.data?.event!!

            progress_bar_details.visibility = View.GONE

            if (!response.hasErrors()) {
                if (event.beforeSurvey.isNullOrEmpty()) {
                    btn_pre_event.alpha = .5f
                    btn_pre_event.isClickable = false
                } else {
                    btn_pre_event.setOnClickListener {
                        val uri: Uri = Uri.parse(event.beforeSurvey)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                }
                if (event.afterSurvey.isNullOrEmpty()) {
                    btn_post_event.alpha = .5f
                    btn_post_event.isClickable = false
                } else {
                    btn_post_event.setOnClickListener {
                        val uri: Uri = Uri.parse(event.afterSurvey)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                }

                val odt = OffsetDateTime.now()
                val zoneOffset = odt.offset
                val dateInMilli = event.date?.atOffset(zoneOffset)?.toInstant()?.toEpochMilli()
                if (dateInMilli is Long) {
                    val displayDate = if (event.date !== null) formatDateTime(view, dateInMilli, FORMAT_SHOW_TIME or FORMAT_ABBREV_TIME or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY) else null
                    val displayTime = if (event.date !== null) formatDateTime(view, dateInMilli, FORMAT_SHOW_TIME) else null

                    tv_event_det_time.text = displayTime
                    tv_event_det_date.text = displayDate
                }
                tv_company_name.text = event.company?.name
                tv_company_location.text = if (event.location.isNullOrEmpty()) getString(R.string.no_location_available) else event.location
                tv_private_description.text = if (event.privateDescription.isNullOrEmpty()) getString(R.string.no_description_available) else event.privateDescription
                presenter = EventDetailPresenter(view)

            }
        }
    }

    companion object {
        fun makeIntent(context: Context, eventID: String) = Intent(context, EventDetailActivity::class.java).apply {
            putExtra(IntentExtra.EVENT_ID, eventID)
        }

    }
}
