package com.danilkinkin.buckwheat.data.entities

import java.math.BigDecimal
import java.util.*

data class HistoricalPeriod(
    val periodId: Long,
    val budget: BigDecimal,
    val startDate: Date,
    val endDate: Date,
    val actualEndDate: Date?,
    val transactions: List<Transaction>,
    val totalSpent: BigDecimal,
    val spends: List<Transaction>
) {
    val isCompleted: Boolean
        get() = actualEndDate != null
}