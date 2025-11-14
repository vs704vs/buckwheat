package com.danilkinkin.buckwheat.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType

class FakeTransactionDao: TransactionDao {
    private val spends = mutableListOf<Transaction>()

    override fun getAll(): LiveData<List<Transaction>> {
        return MutableLiveData(spends.toList())
    }

    override fun getAll(type: TransactionType): LiveData<List<Transaction>> {
        return MutableLiveData(spends.filter { it.type == type })
    }

    override fun getAllByPeriod(periodId: Long): LiveData<List<Transaction>> {
        return MutableLiveData(spends.filter { it.periodId == periodId })
    }

    override fun getAllByPeriod(type: TransactionType, periodId: Long): LiveData<List<Transaction>> {
        return MutableLiveData(spends.filter { it.type == type && it.periodId == periodId })
    }

    override fun getAllPeriodIds(): LiveData<List<Long>> {
        return MutableLiveData(spends.map { it.periodId }.distinct().sorted())
    }

    override fun getById(uid: Int): Transaction? {
        return spends.find { it.uid == uid }
    }

    override fun insert(vararg transaction: Transaction) {
        spends.addAll(transaction)
    }

    override fun update(vararg transaction: Transaction) {
        transaction.forEach { updatedTransaction ->
            val index = spends.indexOfFirst { it.uid == updatedTransaction.uid }
            if (index != -1) {
                spends[index] = updatedTransaction
            }
        }
    }

    override fun deleteById(uid: Int) {
        spends.removeIf { it.uid == uid }
    }

    override fun deleteAll() {
        spends.clear()
    }

    override fun deleteByPeriod(periodId: Long) {
        spends.removeIf { it.periodId == periodId }
    }

}