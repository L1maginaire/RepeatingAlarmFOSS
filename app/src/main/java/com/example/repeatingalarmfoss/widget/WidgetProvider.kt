package com.example.repeatingalarmfoss.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.screens.added_tasks.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val ARG_TASK_LIST = "ARG_TASK_LIST"

class WidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var logger: FlightRecorder

    @Inject
    lateinit var taskDao: TaskLocalDataSource

    private val database = CompositeDisposable()

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) = super.onDeleted(context, appWidgetIds).also { database.clear() }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        database.clear()
        if (::taskDao.isInitialized.not()) (context.applicationContext as RepeatingAlarmApp).appComponent.inject(this)

        database += taskDao.getAll() /*todo repository*/
            .timeout(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ taskList ->
                appWidgetIds?.forEach {
                    Log.d("a", "aaa 1")
                    with(RemoteViews(context.packageName, R.layout.collection_widget)) {
                        setRemoteAdapter(R.id.widgetListView, Intent(context, TaskListWidgetService::class.java).apply {
                            putExtra(ARG_TASK_LIST, taskList.map { it.time }.toTypedArray())
                        })
                        appWidgetManager?.updateAppWidget(it, this)
                    }
                }
            }, { logger.e(stackTrace = it.stackTrace) })
    }

    /*todo: another view if size of tasks == 0*/
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager?, appWidgetId: Int) {
        val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }, 0)

//        appWidgetManager?.updateAppWidget(appWidgetId, RemoteViews(context.packageName, R.layout.layout_widget_simple).apply {
//            setOnClickPendingIntent(R.id.widgetImageView, pendingIntent)
//        })
    }
}