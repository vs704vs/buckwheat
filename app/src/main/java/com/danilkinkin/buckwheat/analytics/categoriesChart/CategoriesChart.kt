package com.danilkinkin.buckwheat.analytics.categoriesChart

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.harmonizeWithColor
import com.danilkinkin.buckwheat.util.toPalette
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.countDays
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import com.danilkinkin.buckwheat.analytics.CountDaysChip
import com.danilkinkin.buckwheat.analytics.Arrow
import com.danilkinkin.buckwheat.analytics.Cross
import com.danilkinkin.buckwheat.analytics.growByMiddleChildRowMeasurePolicy
import java.math.BigDecimal
import java.util.Date

data class TagUsage(
    val name: String,
    val amount: BigDecimal,
    var color: HarmonizedColorPalette? = null,
    var isSpecial: Boolean = false,
)

var baseColors = listOf(
    Color(0xFFF86BAE), // Pink
    Color(0xFFF36FFF), // Purple
    Color(0xFFAB96FF), // Light Purple
    Color(0xFF5FC7E7), // Light Blue
    Color(0xFF75E584), // Green
    Color(0xFFFFD386), // Yellow
    Color(0xFFEF7564), // Orange
    Color(0xFF4FC3F7), // Cyan
    Color(0xFFFF8A65), // Deep Orange
    Color(0xFFBA68C8), // Medium Purple
    Color(0xFF81C784), // Light Green
    Color(0xFFFFB74D), // Amber
    Color(0xFF64B5F6), // Blue
    Color(0xFFA1C181), // Olive Green
    Color(0xFFE57373), // Light Red
    Color(0xFF9575CD), // Deep Purple
    Color(0xFF4DB6AC), // Teal
    Color(0xFFDCE775), // Lime
    Color(0xFFFF8A80), // Light Red Accent
    Color(0xFFB39DDB), // Light Purple Accent
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalStdlibApi::class)
@Composable
fun CategoriesChartCard(
    modifier: Modifier = Modifier,
    spends: List<Transaction>,
    currency: ExtendCurrency,
    startDate: Date? = null,
    endDate: Date? = null,
    onTagClick: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val isNightMode = isNightMode()
    val labelWithoutTag = stringResource(R.string.without_tag)
    val labelRest = stringResource(R.string.rest_tags)
    // Show all tags instead of limiting to 7
    val maxDisplay = Int.MAX_VALUE
    
    // Calculate total expenditure
    val totalExpenditure = spends.map { it.value }.fold(BigDecimal.ZERO) { acc, amount -> acc + amount }

    val colors = baseColors.map {
        toPalette(
            color = harmonizeWithColor(
                designColor = it,
                sourceColor = MaterialTheme.colorScheme.primary
            ),
        )
    }
    val restColor = toPalette(
        color = harmonize(
            designColor = Color(0xFF222222),
            sourceColor = MaterialTheme.colorScheme.primary
        ),
    ).copy(
        main = if (isNightMode) Color(0xFFF0F0F0) else Color(0xFF222222),
        onSurface = if (isNightMode) Color(0xFF1A1A1A) else Color(0xFFF4F4F4)
    )
    val stubColor = toPalette(
        color = harmonize(
            designColor = Color(0xFFCCCCCC),
            sourceColor = MaterialTheme.colorScheme.primary
        ),
    ).copy(
        main = if (isNightMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFCCCCCC),
    )

    var offsetColor = 0

    val tags by remember {
        // Convert to TagUsage, group by tag and sum amounts
        var result = spends
            .map { it.copy(comment = it.comment.ifEmpty { labelWithoutTag }) }
            .groupBy { it.comment.trim() }
            .map { tag ->
                TagUsage(
                    tag.key,
                    tag.value.map { it.value }.reduce { acc, next -> acc + next },
                    isSpecial = tag.key == labelWithoutTag,
                )
            }
            .sortedBy { it.amount }
            .reversed()
            .toList()

        // Keep "without tag" in its natural position (sorted by amount)

        // Set colors for all tags
        result.forEachIndexed { index, tagUsage ->
            tagUsage.color = if (tagUsage.name == labelWithoutTag) {
                offsetColor++
                restColor
            } else {
                // Cycle through colors if we have more tags than colors
                colors[(index - offsetColor) % colors.size]
            }
        }

        // Show all tags - no longer combine into "Rest"

        mutableStateOf(result)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = combineColors(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant,
                angle = 0.3f,
            ),
        )
    ) {
        // Header with period and total expenditure
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Period information using reused date range component
            if (startDate != null && endDate != null) {
                Layout(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    measurePolicy = growByMiddleChildRowMeasurePolicy(LocalDensity.current),
                    content = {
                        Column {
                            Text(
                                text = prettyDate(
                                    startDate,
                                    pattern = "dd MMM",
                                    simplifyIfToday = false,
                                ),
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            )
                        }

                        Box(
                            modifier = Modifier
                        ) {
                            Arrow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .fillMaxHeight()
                            )
                            CountDaysChip(
                                Modifier.align(Alignment.Center),
                                fromDate = startDate,
                                toDate = endDate
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = prettyDate(
                                    endDate,
                                    pattern = "dd MMM",
                                    simplifyIfToday = false,
                                ),
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Total expenditure
            Text(
                text = "Total: ${numberFormat(context, totalExpenditure, currency)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        
        if (tags.size == 1 && tags.first().name == labelWithoutTag) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        DonutChart(
                            modifier = Modifier
                                .padding(end = 16.dp, bottom = 8.dp)
                                .size(64.dp),
                            items = listOf(TagUsage("", BigDecimal(360), stubColor)),
                        )
                        Column {
                            Text(
                                text = "We can't split your spends by categories",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Use tags to see chart by categories ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                                    ),
                                )
                            }
                        }
                    }


                }
            }
        } else {
            DonutChart(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .size(64.dp),
                items = tags,
            )
            FlowRow(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                tags.forEach { tag ->
                    TagAmount(
                        modifier = Modifier.padding(4.dp, 4.dp),
                        value = tag.name,
                        amount = tag.amount,
                        palette = tag.color,
                        isSpecial = tag.isSpecial,
                        currency = currency,
                        onClick = onTagClick?.let { { it(tag.name) } },
                    )
                }
            }
        }
    }
}

@Preview(name = "With other", widthDp = 360)
@Composable
private fun PreviewWithOther() {
    val tags = listOf(
        "Food",
        "Transport",
        "Food",
        "",
        "Cinema",
        "Transport",
        "Food",
        "Entertainment",
        "Food",
        "",
        "Transport",
        "Food",
        "Cinema",
        "Transport",
        "Education"
    )

    val startDate = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 days ago
    val endDate = Date()

    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            startDate = startDate,
            endDate = endDate,
            spends = tags.mapIndexed { index, it ->
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(50 + index),
                    date = Date(),
                    comment = it
                )
            },
        )
    }
}

@Preview(name = "Many tags", widthDp = 360)
@Preview(name = "Many tags (Dark mode)", widthDp = 360, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewManyTags() {
    val tags = listOf(
        "Food",
        "Alcohol",
        "Transport",
        "Plants",
        "Food",
        "Bar",
        "Lost",
        "",
        "Cinema",
        "Transport",
        "Food",
        "Subscriptions",
        "Tools",
        "Entertainment",
        "Food",
        "",
        "Transport",
        "Software",
        "Food",
        "Taxes",
        "Transport",
        "Education"
    )

    val startDate = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 days ago
    val endDate = Date()

    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            startDate = startDate,
            endDate = endDate,
            spends = tags.mapIndexed { index, it ->
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(50 + index),
                    date = Date(),
                    comment = it
                )
            },
        )
    }
}

@Preview(name = "Without tags", widthDp = 360)
@Preview(name = "Without tags (Dark mode)", widthDp = 360, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWithoutTags() {
    val startDate = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // 30 days ago
    val endDate = Date()

    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            startDate = startDate,
            endDate = endDate,
            spends = List(10) { "" }.mapIndexed { index, it ->
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(50 + index),
                    date = Date(),
                    comment = it
                )
            },
        )
    }
}