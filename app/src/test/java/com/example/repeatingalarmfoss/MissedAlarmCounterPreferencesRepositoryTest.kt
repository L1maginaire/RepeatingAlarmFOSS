package com.example.repeatingalarmfoss

import android.os.Build
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCounter
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDb
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.helper.rx.TestSchedulers
import com.example.repeatingalarmfoss.repositories.GetMissedAlarmCounterResult
import com.example.repeatingalarmfoss.repositories.MissedAlarmCounterPreferencesRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class MissedAlarmCounterPreferencesRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var db: MissedAlarmsCountersDb
    private lateinit var missedAlarmsCountersDao: MissedAlarmsCountersDao
    private lateinit var missedAlarmCounterPreferencesRepository: MissedAlarmCounterPreferencesRepository

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, MissedAlarmsCountersDb::class.java).allowMainThreadQueries().build()
        missedAlarmsCountersDao = db.getCountersDao()
        missedAlarmCounterPreferencesRepository = MissedAlarmCounterPreferencesRepository(missedAlarmsCountersDao, BaseComposers(TestSchedulers(), FlightRecorder(createTempFile())))
    }

    @After fun tearDown() = db.close()

    @Test
    fun `if entity doesn't exist, create with initial value (1)`() {
        val id = 1L
        assert(missedAlarmsCountersDao.getCount() == 0)
        missedAlarmCounterPreferencesRepository.getAndUpdateMissedAlarmsCounter(id)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(GetMissedAlarmCounterResult.Success(1))
        assert(missedAlarmsCountersDao.getCount() == 1)
    }

    @Test
    fun `if entity exists, and counter value is 1, result should contain 1, but entity should be updated to counter's value 2`() {
        val id = 1L
        missedAlarmsCountersDao.insert(MissedAlarmsCounter(id, 1))
            .test()
            .assertComplete()
        assert(missedAlarmsCountersDao.getCount() == 1)

        missedAlarmCounterPreferencesRepository.getAndUpdateMissedAlarmsCounter(id)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(GetMissedAlarmCounterResult.Success(1))
    }
}