package com.example.repeatingalarmfoss.screens.added_tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.repeatingalarmfoss.databinding.ItemTaskBinding
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate
import com.example.repeatingalarmfoss.helper.rx.DEFAULT_UI_SKIP_DURATION
import com.jakewharton.rxbinding3.view.longClicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer
import java.util.concurrent.TimeUnit

class AddedTasksAdapter(private val longClickCallback: (id: Long) -> Unit) : RecyclerView.Adapter<AddedTasksAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var tasks: MutableList<TaskUi> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addNewTask(task: TaskUi) = tasks.add(task).also { notifyDataSetChanged() }
    fun removeTask(id: Long) = tasks.remove(tasks.first { it.id == id }).also { notifyDataSetChanged() }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = tasks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding?.task = tasks[position]
        clicks += holder.itemView.longClicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .map { tasks[position].id }
            .subscribe { longClickCallback.invoke(it) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemTaskBinding? = DataBindingUtil.bind(containerView)
    }
}

data class TaskUi(val id: Long, val description: String, val time: String) {
    companion object {
        fun testObject(id: Long, description: String, time: String) = TaskUi(id, description, time.toLong().toReadableDate())
    }
}