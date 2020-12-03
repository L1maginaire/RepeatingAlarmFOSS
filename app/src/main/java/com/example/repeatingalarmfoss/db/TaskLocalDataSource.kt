package com.example.repeatingalarmfoss.db

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TaskLocalDataSource {
    /*TODO: classic Repository*/
    @Query("SELECT * FROM task")
    fun getAll(): Single<List<Task>>

    @VisibleForTesting
    @Query("SELECT COUNT(id) FROM task")
    fun getCount(): Int

    @Query("SELECT * FROM task WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): Single<List<Task>>

    @Query("SELECT * FROM task WHERE description LIKE :desc LIMIT 1")
    fun findByName(desc: String): Single<Task>

    @Insert
    fun insert(task: Task): Single<Long>

    @Insert
    fun insertAll(tasks: List<Task>): Completable

    @Query("DELETE FROM task WHERE id = :id")
    fun delete(id: Long): Completable
}