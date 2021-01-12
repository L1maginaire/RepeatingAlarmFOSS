package com.example.repeatingalarmfoss.interactors

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.db.TasksDb
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationResult
import com.example.repeatingalarmfoss.usecases.NextLaunchPreparationUseCase
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val fieldDescription = "desc"
private val classifier = RepeatingClassifier.EVERY_X_TIME_UNIT
private const val classifierValue = "2Minutes"
private const val time = "123"
private const val id = 1L

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class NextLaunchPreparationUseCaseTest {
    private val context: Context = mock(Context::class.java)
    private val logger = mock(FlightRecorder::class.java)
    private val nextLaunchTimeCalculationUseCase = NextLaunchTimeCalculationUseCase(InstrumentationRegistry.getInstrumentation().context)
    private lateinit var nextLaunchPreparationUseCase: NextLaunchPreparationUseCase
    private lateinit var db: TasksDb
    private lateinit var taskDao: TaskLocalDataSource

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, TasksDb::class.java).allowMainThreadQueries().build()
        taskDao = db.taskDao()
        nextLaunchPreparationUseCase = NextLaunchPreparationUseCase(nextLaunchTimeCalculationUseCase, logger, taskDao)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `successful scenario`() {
        assert(taskDao.getCount() == 0)
        val task = Task.testObject(fieldDescription, classifier, classifierValue, time, id)

        val nextLaunchTime = nextLaunchTimeCalculationUseCase.getNextLaunchTime(time.toLong(), classifierValue).toString()
        nextLaunchPreparationUseCase.execute(task, time.toLong()).test().assertComplete().assertNoErrors().assertResult(NextLaunchPreparationResult.Success(task.copy(time = nextLaunchTime)))
        assert(taskDao.getCount() == 1)
        assert(taskDao.getAll().blockingGet()[0].id == id)
    }

    @Test
    fun `if next time launch lesser or equal to current, execution should fail`() {
        assert(taskDao.getCount() == 0)
        val task = Task.testObject(fieldDescription, classifier, classifierValue, time, id)

        val nextLaunchTime = nextLaunchTimeCalculationUseCase.getNextLaunchTime(time.toLong(), classifierValue).toString()
        nextLaunchPreparationUseCase.execute(task, nextLaunchTime.toLong()).test().assertComplete().assertNoErrors().assertResult(NextLaunchPreparationResult.IncorrectNextLaunchTimeError)
        assert(taskDao.getCount() == 0)
    }

    @Test
    fun `when inserting in DB fails, chain should return DatabaseCorruptionError`() {
        val taskDao = mock(TaskLocalDataSource::class.java)
        val task = Task.testObject(fieldDescription, classifier, classifierValue, time, id)
        `when`(taskDao.insert(com.nhaarman.mockitokotlin2.any())).thenReturn(Single.error(RuntimeException()))
        val nextLaunchPreparationUseCase = NextLaunchPreparationUseCase(nextLaunchTimeCalculationUseCase, logger, taskDao)
        nextLaunchPreparationUseCase.execute(task, time.toLong()).test().assertComplete().assertNoErrors().assertResult(NextLaunchPreparationResult.DatabaseCorruptionError)
    }
}