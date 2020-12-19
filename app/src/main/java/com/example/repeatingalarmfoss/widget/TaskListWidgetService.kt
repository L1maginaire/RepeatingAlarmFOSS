package com.example.repeatingalarmfoss.widget

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate

class TaskListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = TaskListWidgetViewsFactory(applicationContext, intent.getStringArrayExtra(ARG_TASK_LIST)!!)

    class TaskListWidgetViewsFactory(private val context: Context, private val dates: Array<String>) : RemoteViewsFactory {
        override fun getViewAt(position: Int): RemoteViews? = if (position == AdapterView.INVALID_POSITION || dates.isEmpty()) {
            null
        } else {
            RemoteViews(context.packageName, R.layout.collection_widget_list_item).apply { setTextViewText(R.id.widgetItemTaskNameLabel, dates[position].toLong().toReadableDate()) }
        }

        override fun onCreate() = Unit
        override fun onDataSetChanged() = Binder.restoreCallingIdentity(Binder.clearCallingIdentity())
        override fun onDestroy() = Unit
        override fun getCount() = dates.size
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount(): Int = 1
        override fun hasStableIds() = true
        override fun getItemId(position: Int): Long = position.toLong()
    }
}