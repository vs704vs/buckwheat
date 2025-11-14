package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date ASC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date ASC")
    fun getAll(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE period_id = :periodId ORDER BY date ASC")
    fun getAllByPeriod(periodId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type AND period_id = :periodId ORDER BY date ASC")
    fun getAllByPeriod(type: TransactionType, periodId: Long): LiveData<List<Transaction>>

    @Query("SELECT DISTINCT period_id FROM transactions ORDER BY period_id DESC")
    fun getAllPeriodIds(): LiveData<List<Long>>

    @Query("SELECT * FROM transactions WHERE uid = :uid")
    fun getById(uid: Int): Transaction?

    @Insert
    fun insert(vararg transaction: Transaction)

    @Update(entity = Transaction::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg transaction: Transaction)

    @Query("DELETE FROM transactions WHERE uid = :uid")
    fun deleteById(uid: Int)

    @Query("DELETE FROM transactions")
    fun deleteAll()

    @Query("DELETE FROM transactions WHERE period_id = :periodId")
    fun deleteByPeriod(periodId: Long)
}
