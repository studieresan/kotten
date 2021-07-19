package com.studieresan.studs.events.adapters

import EventsQuery
import android.content.Context
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studieresan.studs.R
import com.studieresan.studs.util.exhaustive
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

private const val FUTURE_EVENT_TITLE = 0
private const val PAST_EVENT_TITLE = 1
private const val FUTURE_EVENT = 2
private const val PAST_EVENT = 3
private const val NEXT_EVENT_TITLE = 4
private const val NEXT_EVENT = 5
private const val EMPTY = 6

class EventAdapter(
        val events: List<EventsQuery.Event>,
        private val applicationContext: Context,
        private val didSelectEventCallback: (EventsQuery.Event) -> Unit
) : ListAdapter<EventsQuery.Event, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var pastEvents = emptyList<EventsQuery.Event>()
    private var futureEvents = emptyList<EventsQuery.Event>()

    init {
        submitList(events)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        fun inflate(@LayoutRes view: Int) = LayoutInflater.from(parent.context).inflate(view, parent, false)

        return when (viewType) {
            FUTURE_EVENT_TITLE, PAST_EVENT_TITLE, NEXT_EVENT_TITLE -> EventTitleViewHolder(inflate(R.layout.list_item_event_title), viewType)
            NEXT_EVENT -> NextEventViewHolder(inflate(R.layout.list_item_next_event_card))
            PAST_EVENT, FUTURE_EVENT -> EventViewHolder(inflate(R.layout.list_item_event_card))
            EMPTY -> EmptyViewHolder(inflate(R.layout.list_item_empty))
            else -> throw IllegalStateException("Unsupported view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        when (holder) {
            is EventViewHolder -> if (type == PAST_EVENT) {
                if (futureEvents.isNotEmpty()) {
                    holder.bind(pastEvents[position - 4 - futureEvents.size])
                } else {
                    holder.bind(getItem(position - 4))
                }
            } else {
                holder.bind(futureEvents[position - 3])
            }
            is NextEventViewHolder -> holder.bind(futureEvents.first())
            is EventTitleViewHolder -> holder.bind()
            else -> Unit /* Do nothing */
        }.exhaustive
    }

    override fun submitList(list: List<EventsQuery.Event>?) {
        list?.let {
            partitionEvents(it)
        }
        super.submitList(list)
    }

    override fun getItemCount(): Int {
        val size = super.getItemCount()
        return if (size == 0) {
            0
        } else {
            size + 4
        }
    }

    private fun partitionEvents(events: List<EventsQuery.Event>) {
        val today: LocalDateTime = LocalDateTime.now()
        val (past, future) = events.partition { it.date!! < today }
        pastEvents = past
        futureEvents = future.reversed()
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> NEXT_EVENT_TITLE
        1 -> if (futureEvents.isNotEmpty()) {
            NEXT_EVENT
        } else {
            EMPTY
        }
        2 -> FUTURE_EVENT_TITLE
        in 2..futureEvents.size + 2 -> if (futureEvents.isNotEmpty()) {
            FUTURE_EVENT
        } else {
            EMPTY
        }
        futureEvents.size + 3 -> PAST_EVENT_TITLE
        else -> if (pastEvents.isNotEmpty()) {
            PAST_EVENT
        } else {
            EMPTY
        }
    }

    inner class NextEventViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val companyName: TextView = view.findViewById(R.id.tv_company_name)
        private val date: TextView = view.findViewById(R.id.tv_date)
        private val time: TextView = view.findViewById(R.id.tv_time)
        private val location: TextView = view.findViewById(R.id.tv_location_address)
        private val description: TextView = view.findViewById(R.id.tv_event_description)

        fun bind(event: EventsQuery.Event) {
            view.tag = this
            view.setOnClickListener { didSelectEventCallback.invoke(event) }

            val odt = OffsetDateTime.now()
            val zoneOffset = odt.offset
            val dateInMilli = event.date?.atOffset(zoneOffset)?.toInstant()?.toEpochMilli()
            if (dateInMilli is Long) {
                val displayDate = DateUtils.formatDateTime(applicationContext, dateInMilli, DateUtils.FORMAT_ABBREV_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY).capitalize()
                val displayTime = DateUtils.formatDateTime(applicationContext, dateInMilli, DateUtils.FORMAT_SHOW_TIME)
                date.text = displayDate
                time.text = displayTime
            }

            companyName.text = event.company?.name

            if (!event.location.isNullOrEmpty()) {
                location.text = event.location
            }
            if (!event.publicDescription.isNullOrEmpty()) {
                description.text = event.privateDescription
            }
        }

    }

    inner class EventViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val companyName: TextView = view.findViewById(R.id.tv_company_name)
        private val month: TextView = view.findViewById(R.id.tv_date)
        private val day: TextView = view.findViewById(R.id.tv_event_det_time)

        fun bind(event: EventsQuery.Event) {
            view.setOnClickListener { didSelectEventCallback.invoke(event) }
            companyName.text = event.company?.name
            if (event.date !== null) {
                month.text = event.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                day.text = event.date.dayOfMonth.toString()
            }
        }
    }

    inner class EventTitleViewHolder(private val view: View, private val viewType: Int) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tv_header)

        fun bind() {
            title.text = view.context.getString(when (viewType) {
                FUTURE_EVENT_TITLE -> R.string.upcoming_events
                PAST_EVENT_TITLE -> R.string.past_events
                else -> R.string.next_event
            })
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (viewType == NEXT_EVENT_TITLE) 36F else 28F)
        }
    }

    inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EventsQuery.Event>() {
            override fun areItemsTheSame(oldItem: EventsQuery.Event, newItem: EventsQuery.Event): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EventsQuery.Event, newItem: EventsQuery.Event): Boolean = oldItem == newItem
        }
    }
}
