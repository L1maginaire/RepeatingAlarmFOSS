package com.example.repeatingalarmfoss.screens.added_tasks

import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import io.reactivex.Single
import javax.inject.Inject

class TaskInteractor @Inject constructor(private val taskLocalDataSource: TaskLocalDataSource, private val nextLaunchTimeCalculationUseCase: NextLaunchTimeCalculationUseCase, private val baseComposers: BaseComposers) {
    fun fetchTasks(): Single<FetchTasksResult> = taskLocalDataSource.getAll()
        .flattenAsObservable { it.map { task -> TaskUi(id = task.id, description = task.description, time = task.time.toLong().toReadableDate()) } } /*FIXME mapping should be placed in repository*/
        .toList()
        .map<FetchTasksResult> { FetchTasksResult.Success(it) }
        .onErrorReturn { FetchTasksResult.Failure.DatabaseCorruption }
        .compose(baseComposers.commonSingleFetchTransformer())

    fun addTask(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String): Single<AddTaskResult> =
        Single.just(
            com.example.repeatingalarmfoss.db.Task(
                description,
                repeatingClassifier,
                repeatingClassifierValue,
                if (repeatingClassifier == RepeatingClassifier.DAY_OF_WEEK) nextLaunchTimeCalculationUseCase.getNextLaunchTime(time, repeatingClassifierValue).toString() else time
            )
        ).flatMap { task -> taskLocalDataSource.insert(task).map { task.apply { id = it } } }
            .map<AddTaskResult> { AddTaskResult.Success(it to TaskUi(it.id, it.description, it.time.toLong().toReadableDate())) } /*FIXME mapping should be placed in repository*/
            .onErrorReturn { AddTaskResult.Failure.DatabaseCorruption }
            .compose(baseComposers.commonSingleFetchTransformer())

    fun delete(id: Long): Single<DeleteTaskResult> = taskLocalDataSource.delete(id)
        .toSingle<DeleteTaskResult> { DeleteTaskResult.Success(id) }
        .onErrorReturn { DeleteTaskResult.DatabaseCorruptionError }
        .compose(baseComposers.commonSingleFetchTransformer())
}

sealed class DeleteTaskResult {
    data class Success(val id: Long) : DeleteTaskResult()
    object DatabaseCorruptionError : DeleteTaskResult()
}

sealed class FetchTasksResult {
    data class Success(val tasks: List<TaskUi>) : FetchTasksResult()
    sealed class Failure : FetchTasksResult() {
        object DatabaseCorruption : Failure()
    }
}

sealed class AddTaskResult {
    data class Success(val task: Pair<com.example.repeatingalarmfoss.db.Task, TaskUi>) : AddTaskResult()
    sealed class Failure : AddTaskResult() {
        object DatabaseCorruption : Failure()
    }
}