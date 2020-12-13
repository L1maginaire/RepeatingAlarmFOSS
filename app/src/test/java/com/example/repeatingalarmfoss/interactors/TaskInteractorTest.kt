package com.example.repeatingalarmfoss.interactors

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.db.TasksDb
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.helper.rx.TestSchedulers
import com.example.repeatingalarmfoss.screens.added_tasks.*
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class TaskInteractorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var taskDao: TaskLocalDataSource
    private lateinit var db: TasksDb
    private lateinit var taskInteractor: TaskInteractor
    private val nextLaunchTimeCalculationUseCase = NextLaunchTimeCalculationUseCase(context)
    private val schedulers = TestSchedulers()

    /** allowMainThreadQueries() is tricky, but COUNT doesn't work anyhow*/
    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, TasksDb::class.java).allowMainThreadQueries().build()
        taskDao = db.taskDao()
        taskInteractor = TaskInteractor(taskDao, nextLaunchTimeCalculationUseCase, BaseComposers(schedulers, FlightRecorder(createTempFile())))
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun `adding and deleting`() {
        val fieldDescription = "desc"
        val classifier = RepeatingClassifier.EVERY_X_TIME_UNIT
        val classifierValue = "value"
        val time = "123"
        val id = 1L

        val task = Task.testObject(fieldDescription, classifier, classifierValue, time, id)
        val taskUi = TaskUi.testObject(id, fieldDescription, time)
        assert(taskDao.getCount() == 0)
        taskInteractor.addTask(fieldDescription, classifier, classifierValue, time).test().assertNoErrors().assertComplete().assertResult(AddTaskResult.Success(task to taskUi))
        assert(taskDao.getCount() == 1)

        taskInteractor.delete(id).test().assertComplete().assertNoErrors().assertResult(DeleteTaskResult.Success(id))
        assert(taskDao.getCount() == 0)
    }

    @Test
    fun `fetching all`(){
        val taskList = mutableListOf<Task>()
        val taskUiList = mutableListOf<TaskUi>()
        for (i in 1 until 100) {
            val fieldDescription = "desc"
            val classifier = RepeatingClassifier.EVERY_X_TIME_UNIT
            val classifierValue = "value"

            Thread.sleep(1)
            val time = System.currentTimeMillis().toString()

            val id = i.toLong()

            taskList.add(Task.testObject(fieldDescription, classifier, classifierValue, time, id))
            taskUiList.add(TaskUi.testObject(id, fieldDescription, time))
        }
        taskDao.insertAll(taskList).test().assertNoErrors().assertComplete()
        taskInteractor.fetchTasks().test().assertNoErrors().assertComplete().assertResult(FetchTasksResult.Success(taskUiList))
    }
}