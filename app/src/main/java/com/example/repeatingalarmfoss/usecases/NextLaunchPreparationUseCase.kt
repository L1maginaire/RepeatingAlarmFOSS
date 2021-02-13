package com.example.repeatingalarmfoss.usecases

import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate
import io.reactivex.Single
import javax.inject.Inject

class NextLaunchPreparationUseCase
@Inject constructor(private val nextLaunchTimeCalculationUseCase: NextLaunchTimeCalculationUseCase, private val logger: FlightRecorder, private val taskLocalDataSource: TaskLocalDataSource) {
    fun execute(task: Task, now: Long = System.currentTimeMillis()): Single<NextLaunchPreparationResult> = Single.just(task.repeatingClassifier)
        .map {
            when (it) {
                RepeatingClassifier.EVERY_X_TIME_UNIT -> nextLaunchTimeCalculationUseCase.getNextLaunchTime(task.time.toLong(), task.repeatingClassifierValue)
                RepeatingClassifier.DAY_OF_WEEK -> nextLaunchTimeCalculationUseCase.getNextLaunchTime(task.time, task.repeatingClassifierValue)
                else -> throw IllegalStateException()
            }
        }.doOnSuccess {
            if (it < now) with("nextLaunchTime (${it.toReadableDate()}) is lesser than now (${now.toReadableDate()})") {
                println("a")
                logger.wtf { this }
                throw IllegalStateException(this)
            }
        }.doOnError {
            logger.e(label = javaClass.simpleName, stackTrace = it.stackTrace)
        }.map {
            task.copy(time = it.toString())
        }.flatMap { taskWithUpdatedLaunchTime ->
            taskLocalDataSource.insert(taskWithUpdatedLaunchTime)
                .map<NextLaunchPreparationResult> { NextLaunchPreparationResult.Success(taskWithUpdatedLaunchTime) }
                .doOnError { logger.wtf { "${javaClass.simpleName} couldn't save Task into database" } }
                .onErrorReturn { NextLaunchPreparationResult.DatabaseCorruptionError }
                .doOnSuccess { logger.i(what = { "(${task.description}) Next launch: ${taskWithUpdatedLaunchTime.time.toLong().toReadableDate()}" }) }
        }.onErrorReturn { NextLaunchPreparationResult.IncorrectNextLaunchTimeError }
}

sealed class NextLaunchPreparationResult {
    data class Success(val newTask: Task) : NextLaunchPreparationResult()
    object DatabaseCorruptionError : NextLaunchPreparationResult()
    object IncorrectNextLaunchTimeError : NextLaunchPreparationResult()
}