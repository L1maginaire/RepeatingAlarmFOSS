package com.example.repeatingalarmfoss.db.missed_alarms_counter

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface MissedAlarmsCountersDao {
    @Query("SELECT * FROM missedalarmscounter")
    fun getAll(): Single<List<MissedAlarmsCounter>>

    @VisibleForTesting
    @Query("SELECT COUNT(id) FROM missedalarmscounter")
    fun getCount(): Int

    @Query("SELECT * FROM missedalarmscounter WHERE taskId LIKE :taskId LIMIT 1")
    fun findByTaskId(taskId: Long): Single<MissedAlarmsCounter>

    @Query("SELECT * FROM missedalarmscounter WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): Single<List<MissedAlarmsCounter>>

    @Insert(onConflict = REPLACE)
    fun insert(counter: MissedAlarmsCounter): Single<Long>

    @Insert
    fun insertAll(counters: List<MissedAlarmsCounter>): Completable

    @Query("DELETE FROM missedalarmscounter WHERE id = :id")
    fun delete(id: Long): Completable
}