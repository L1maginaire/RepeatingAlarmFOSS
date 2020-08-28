package com.example.repeatingalarmfoss.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TaskRepository {
    /*TODO: classic Repository*/
    @Query("SELECT * FROM task")
    fun getAll(): Single<List<Task>>

    @Query("SELECT * FROM task WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): Single<List<Task>>

    @Query("SELECT * FROM task WHERE description LIKE :desc LIMIT 1")
    fun findByName(desc: String): Single<Task>

    @Insert
    fun insert(task: Task): Single<Long>

    @Query("DELETE FROM task WHERE id = :id")
    fun delete(id: Long): Completable
}