package com.example.repeatingalarmfoss

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.longClicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_add_task.*
import java.util.concurrent.TimeUnit

class TasksAdapter(private val longClickCallback: (id: Long) -> Unit) : RecyclerView.Adapter<TasksAdapter.ViewHolder>() {
    private val clicks = CompositeDisposable()
    var tasks: MutableList<Task> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addNewTask(description: String) = tasks.add(Task(tasks.lastIndex+1L, description))
    fun removeTask(id: Long) = tasks.remove(tasks.first { it.id == id })

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()
    override fun getItemCount() = tasks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_add_task))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(tasks[position]).also {
        clicks += holder.itemView.longClicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .map { tasks[position].id }
            .subscribe { longClickCallback.invoke(it) }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(task: Task) {
            taskDescriptionTv.text = task.description
        }
    }
}

data class Task(val id: Long, val description: String)