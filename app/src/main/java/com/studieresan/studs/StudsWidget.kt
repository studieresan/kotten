package com.studieresan.studs

import EventsQuery
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.studieresan.studs.graphql.apolloClient
import com.studieresan.studs.net.StudsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class StudsWidget : AppWidgetProvider() {

    @Inject
    lateinit var studsRepository: StudsRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        StudsApplication.applicationComponent.inject(this)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

        // Adds on-click support
        val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.studs_widget).apply {
            setOnClickPendingIntent(R.id.widget, pendingIntent)
        }

        CoroutineScope(Dispatchers.Main).launch {
            val response = try {
                apolloClient(context).query(EventsQuery()).await()
            } catch (e: ApolloException) {
                null
            }

            val events = response?.data?.events?.filterNotNull()

            if (events != null && !response.hasErrors()) {
                val today = LocalDateTime.now()
                val orderedEvents = events.sortedBy { it.date }
                val nextEvent =
                        orderedEvents.firstOrNull { if (it.date != null) it.date.toLocalDate() >= today.toLocalDate() else false }

                if (nextEvent !== null) {

                    val odt = OffsetDateTime.now()
                    val zoneOffset = odt.offset
                    val dateInMilli = nextEvent.date?.atOffset(zoneOffset)?.toInstant()?.toEpochMilli()
                    val todayInMilli = today.atOffset(zoneOffset).toInstant().toEpochMilli()
                    if (dateInMilli is Long) {
                        val displayDate = formatDateTime(context, dateInMilli, FORMAT_SHOW_TIME or FORMAT_ABBREV_TIME or FORMAT_SHOW_DATE or FORMAT_NUMERIC_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_ABBREV_MONTH).capitalize()
                        val countdown = getRelativeTimeSpanString(dateInMilli, todayInMilli, DAY_IN_MILLIS)

                        views.setTextViewText(R.id.widget_countdown, countdown)
                        views.setTextViewText(R.id.widget_date, displayDate)
                    }
                    views.setTextViewText(R.id.widget_company, nextEvent.company?.name)

                } else {
                    views.setTextViewText(R.id.widget_company, "No upcoming events")
                }

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

    }

    companion object {
    }
}

