package com.example.officetracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.officetracker.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.officetracker.data.repository.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.app.PendingIntent
import android.content.Intent
import com.example.officetracker.MainActivity

@AndroidEntryPoint
class OfficeStatusWidget : AppWidgetProvider() {

    @Inject
    lateinit var repository: AttendanceRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, repository, scope)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        job.cancel()
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            repository: AttendanceRepository,
            scope: CoroutineScope
        ) {
            scope.launch {
                val activeSession = repository.getCurrentActiveSession().first()
                val isOffice = activeSession != null
                
                val views = RemoteViews(context.packageName, R.layout.widget_office_status)
                
                if (isOffice) {
                    views.setTextViewText(R.id.widget_status_text, "In Office")
                    // Timer would need frequent updates, for now just show "Active"
                    // Or we could use Chronometer if supported in RemoteViews (it is!)
                    // value is based on system clock
                    // views.setChronometer(R.id.widget_timer, activeSession.startTime, null, true)
                     views.setTextViewText(R.id.widget_timer_text, "Tracking...")
                     views.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher) // Placeholder
                } else {
                    views.setTextViewText(R.id.widget_status_text, "Away")
                    views.setTextViewText(R.id.widget_timer_text, "--:--")
                    views.setImageViewResource(R.id.widget_icon, R.drawable.ic_status_away)
                }
                
                // Click to open app
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_status_text, pendingIntent)

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
