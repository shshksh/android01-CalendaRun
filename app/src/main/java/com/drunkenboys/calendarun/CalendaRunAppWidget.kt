package com.drunkenboys.calendarun

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.drunkenboys.calendarun.util.localDateToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendaRunAppWidget : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val remoteViews = RemoteViews(context.packageName, R.layout.calendarun_app_widget)

        val widgetText = LocalDate.now().localDateToString()
        remoteViews.setTextViewText(R.id.tv_appWidget_date, widgetText)

        val serviceIntent = Intent(context, CalendaRunRemoteViewsService::class.java)
        remoteViews.setRemoteAdapter(R.id.lv_appWidget_scheduleList, serviceIntent)

        val updateIntent = Intent(context, CalendaRunAppWidget::class.java)
            .setAction(context.getString(R.string.ACTION_BTN_CLICK))
        val pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.btn_appWidget_update, pendingIntent)

        coroutineScope.launch {
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_appWidget_scheduleList)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (action.equals(context.getString(R.string.ACTION_BTN_CLICK))) {
            // TODO: 2021-11-30 listview 갱신
        }
    }
}
