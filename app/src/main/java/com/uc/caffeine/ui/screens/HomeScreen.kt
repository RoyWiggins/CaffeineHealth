package com.uc.caffeine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import com.uc.caffeine.ui.components.segmentedListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uc.caffeine.LocalSnackbarHostState
import com.uc.caffeine.R
import com.uc.caffeine.data.HomeViewMode
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.HeadacheEntry
import com.uc.caffeine.data.model.HeadacheSeverity
import com.uc.caffeine.data.model.DrinkUnit
import com.uc.caffeine.ui.components.CaffeineChart
import com.uc.caffeine.ui.components.CaffeineRadialView
import com.uc.caffeine.ui.components.CaffeineScreenScaffold
import com.uc.caffeine.ui.components.ConsumptionContributionChart
import com.uc.caffeine.ui.components.ConsumptionTimingSection
import com.uc.caffeine.ui.components.DateTimePickerDialog
import com.uc.caffeine.ui.components.DrinkIcon
import com.uc.caffeine.ui.components.ExpressiveIconBadge
import com.uc.caffeine.ui.components.RollingNumberText
import com.uc.caffeine.ui.components.ServingQuantityStepper
import com.uc.caffeine.ui.components.ServingUnitSelector
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.ui.components.WhatsNewSheet
import com.uc.caffeine.ui.components.shimmerEffect
import com.uc.caffeine.ui.theme.CaffeineSurfaceDefaults
import com.uc.caffeine.ui.viewmodel.CaffeineViewModel
import com.uc.caffeine.ui.viewmodel.HomeScreenUiEvent
import com.uc.caffeine.util.ConsumptionContributionDetail
import com.uc.caffeine.util.calculateServingTotalCaffeine
import com.uc.caffeine.util.findMatchingUnit
import com.uc.caffeine.util.formatConsumptionDateHeader
import com.uc.caffeine.util.formatDurationMinutes
import com.uc.caffeine.util.formatServingSummary
import com.uc.caffeine.util.formatTimeOfDay
import com.uc.caffeine.util.formatTimestampToDateTime
import com.uc.caffeine.util.formatTimestampToTime
import com.uc.caffeine.util.HomeTimelineItem
import com.uc.caffeine.util.resolvedZoneId
import java.time.LocalDate
import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ToggleButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

private const val DetailContentFadeInDurationMillis = 180
private const val DetailContentFadeInDelayMillis = 40
private const val DetailContentFadeOutDurationMillis = 80


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    viewModel: CaffeineViewModel = viewModel()
) {
    // Grab the global snackbar instance!
    val snackbarHostState = LocalSnackbarHostState.current

    val currentLevel by viewModel.currentCaffeineLevel.collectAsStateWithLifecycle()
    val liveNowMillis by viewModel.liveCurrentTimeMillis.collectAsStateWithLifecycle()
    val bedtimeForecast by viewModel.caffeineAtBedtime.collectAsStateWithLifecycle()
    val wakeForecast by viewModel.caffeineAtWakeTime.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val isConsumptionEntriesLoading by viewModel.isConsumptionEntriesLoading.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val groupedConsumptionEntries by viewModel.groupedConsumptionEntries.collectAsStateWithLifecycle()
    val homeTimeline by viewModel.homeTimeline.collectAsStateWithLifecycle()
    val showWhatsNew by viewModel.showWhatsNew.collectAsStateWithLifecycle()
    val radialData by viewModel.radialCaffeineData.collectAsStateWithLifecycle()

    var selectedEntry by remember { mutableStateOf<ConsumptionEntry?>(null) }
    var selectedHeadache by remember { mutableStateOf<HomeTimelineItem.Headache?>(null) }
    var showReportHeadache by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = rememberAppHaptics()

    LaunchedEffect(viewModel, snackbarHostState, sheetState) {
        viewModel.homeScreenEvents.collectLatest { event ->
            if (sheetState.isVisible) {
                sheetState.hide()
            }
            selectedEntry = null
            snackbarHostState.currentSnackbarData?.dismiss()

            when (event) {
                is HomeScreenUiEvent.LogActionCompleted -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    CaffeineScreenScaffold(
        title = stringResource(R.string.home_title),
        actions = {
            IconButton(onClick = { haptics.toggle(); showReportHeadache = true }) {
                Icon(
                    imageVector = Icons.Filled.Sick,
                    contentDescription = stringResource(R.string.headache_report_cd),
                    modifier = Modifier.size(20.dp),
                )
            }
            val modes = HomeViewMode.entries
            modes.forEachIndexed { index, mode ->
                ToggleButton(
                    checked = userSettings.homeViewMode == mode,
                    onCheckedChange = { if (it) { haptics.toggle(); viewModel.updateHomeViewMode(mode) } },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    Icon(
                        imageVector = when (mode) {
                            HomeViewMode.GRAPH -> Icons.AutoMirrored.Filled.ShowChart
                            HomeViewMode.CIRCULAR -> Icons.Filled.DonutLarge
                        },
                        contentDescription = when (mode) {
                            HomeViewMode.GRAPH -> stringResource(R.string.home_view_toggle_to_graph_cd)
                            HomeViewMode.CIRCULAR -> stringResource(R.string.home_view_toggle_to_circular_cd)
                        },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    ) { bottomPadding ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = CaffeineSurfaceDefaults.chartContainerColor,
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isConsumptionEntriesLoading) {
                    ContainedLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    AnimatedContent(
                        targetState = userSettings.homeViewMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, delayMillis = 90))) togetherWith
                                fadeOut(animationSpec = tween(90))
                        },
                        label = "home_view_mode",
                    ) { viewMode ->
                        when (viewMode) {
                            HomeViewMode.GRAPH -> CaffeineChart(
                                chartData = chartData,
                                modelProducer = viewModel.chartModelProducer,
                                userSettings = userSettings,
                                liveNowMillis = liveNowMillis,
                                currentCaffeineLevel = currentLevel,
                                predictedBedtimeCaffeineLevel = bedtimeForecast.first,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 12.dp),
                                onEntryClick = { entryId ->
                                    val entry = groupedConsumptionEntries.values
                                        .flatten()
                                        .find { it.id == entryId }
                                    if (entry != null) {
                                        haptics.navigation()
                                        selectedEntry = entry
                                    }
                                },
                                onHeadacheClick = { headacheId ->
                                    val item = homeTimeline.values
                                        .flatten()
                                        .filterIsInstance<HomeTimelineItem.Headache>()
                                        .find { it.entry.id == headacheId }
                                    if (item != null) {
                                        haptics.navigation()
                                        selectedHeadache = item
                                    }
                                },
                            )
                            HomeViewMode.CIRCULAR -> CaffeineRadialView(
                                data = radialData,
                                userSettings = userSettings,
                                nowMillis = liveNowMillis,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        // Dismissal resets each night (keyed on the upcoming wake time).
        var withdrawalDismissed by remember(wakeForecast.second) { mutableStateOf(false) }
        val millisUntilWake = wakeForecast.second - liveNowMillis
        val withinWakeWindow = millisUntilWake in 0..(10L * 60 * 60 * 1000)
        val showWithdrawalWarning = userSettings.withdrawalThresholdEnabled &&
            currentLevel > 0.0 &&
            wakeForecast.first < userSettings.withdrawalThresholdMg &&
            withinWakeWindow &&
            !withdrawalDismissed
        AnimatedVisibility(visible = showWithdrawalWarning) {
            WithdrawalForecastCard(
                caffeineAtWakeMg = wakeForecast.first,
                wakeTimeMillis = wakeForecast.second,
                userSettings = userSettings,
                onDismiss = { withdrawalDismissed = true },
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_my_consumptions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
        ) {
            if (isConsumptionEntriesLoading) {
                item(key = "history-loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ElevatedCard {
                            Box(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ContainedLoadingIndicator()
                            }
                        }
                    }
                }
            } else if (homeTimeline.isEmpty()) {
                item(key = "history-empty") {
                    Text(
                        text = stringResource(R.string.home_no_consumptions),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                homeTimeline.entries.forEachIndexed { index, (date, itemsForDay) ->
                    if (index > 0) {
                        item(
                            key = "history-gap-$date",
                            contentType = "history-gap",
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    stickyHeader(key = "history-header-$date") {
                        val dayTotalMg = itemsForDay
                            .filterIsInstance<HomeTimelineItem.Drink>()
                            .sumOf { it.entry.caffeineMg }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatTimelineHeaderText(
                                        date = date,
                                        settings = userSettings,
                                        referenceTimeMillis = chartData.currentTimeMillis,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.caffeine_mg, dayTotalMg),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    itemsForDay.forEachIndexed { itemIndex, timelineItem ->
                        item(
                            key = when (timelineItem) {
                                is HomeTimelineItem.Drink -> "history-drink-${timelineItem.entry.id}"
                                is HomeTimelineItem.Headache -> "history-headache-${timelineItem.entry.id}"
                            },
                            contentType = "history-entry",
                        ) {
                            when (timelineItem) {
                                is HomeTimelineItem.Drink -> ConsumptionHistoryListItem(
                                    entry = timelineItem.entry,
                                    index = itemIndex,
                                    count = itemsForDay.size,
                                    userSettings = userSettings,
                                    onClick = {
                                        haptics.navigation()
                                        selectedEntry = timelineItem.entry
                                    },
                                    modifier = Modifier.heightIn(min = 65.dp),
                                )
                                is HomeTimelineItem.Headache -> HeadacheHistoryListItem(
                                    item = timelineItem,
                                    index = itemIndex,
                                    count = itemsForDay.size,
                                    userSettings = userSettings,
                                    onClick = {
                                        haptics.navigation()
                                        selectedHeadache = timelineItem
                                    },
                                    modifier = Modifier.heightIn(min = 65.dp),
                                )
                            }
                        }

                        if (itemIndex < itemsForDay.lastIndex) {
                            item(
                                key = "history-item-gap-$date-$itemIndex",
                                contentType = "history-entry-gap",
                            ) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        val detailSnapshotTimeMillis = remember(entry.id) {
            chartData.currentTimeMillis
        }
        val canRevealDetailContent = true

        val detail by produceState<ConsumptionContributionDetail?>(
            initialValue = null,
            key1 = entry.id,
            key2 = userSettings,
        ) {
            value = withContext(Dispatchers.Default) {
                viewModel.getContributionDetail(
                    entry = entry,
                    currentTimeMillis = detailSnapshotTimeMillis
                )
            }
        }

        var isEditing by remember(entry.id) { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { selectedEntry = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "edit-mode-transition",
            ) { editing ->
                if (editing) {
                    EditConsumptionEntrySheet(
                        entry = entry,
                        viewModel = viewModel,
                        userSettings = userSettings,
                        onBack = { isEditing = false },
                        onSave = { quantity, unit, startedAtMillis, durationMinutes ->
                            viewModel.updateLoggedEntry(
                                entry = entry,
                                quantity = quantity,
                                unit = unit,
                                startedAtMillis = startedAtMillis,
                                durationMinutes = durationMinutes,
                            )
                        }
                    )
                } else {
                    ConsumptionLogDetailSheet(
                        entry = entry,
                        detail = detail,
                        canRevealDetailContent = canRevealDetailContent,
                        userSettings = userSettings,
                        onEdit = { isEditing = true },
                        onDuplicate = { viewModel.duplicateLoggedEntry(entry) },
                        onDelete = { viewModel.deleteLoggedEntry(entry) },
                        onMarkTaken = {
                            viewModel.markEntryTaken(entry)
                            selectedEntry = null
                        },
                    )
                }
            }
        }
    }

    if (showReportHeadache) {
        ModalBottomSheet(
            onDismissRequest = { showReportHeadache = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            ReportHeadacheSheet(
                userSettings = userSettings,
                onSubmit = { startedAtMillis, severity, note ->
                    viewModel.reportHeadache(startedAtMillis, severity, note)
                    showReportHeadache = false
                },
            )
        }
    }

    selectedHeadache?.let { headache ->
        ModalBottomSheet(
            onDismissRequest = { selectedHeadache = null },
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            HeadacheDetailSheet(
                item = headache,
                userSettings = userSettings,
                onDelete = {
                    viewModel.deleteHeadache(headache.entry)
                    selectedHeadache = null
                },
            )
        }
    }

    if (showWhatsNew) {
        WhatsNewSheet(onDismiss = { viewModel.markWhatsNewSeen() })
    }
}

@Composable
private fun headacheSeverityLabel(severity: HeadacheSeverity): String = when (severity) {
    HeadacheSeverity.MILD -> stringResource(R.string.headache_severity_mild)
    HeadacheSeverity.MODERATE -> stringResource(R.string.headache_severity_moderate)
    HeadacheSeverity.SEVERE -> stringResource(R.string.headache_severity_severe)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeadacheHistoryListItem(
    item: HomeTimelineItem.Headache,
    index: Int,
    count: Int,
    userSettings: UserSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            ExpressiveIconBadge(
                index = index,
                size = 44.dp,
            ) {
                Text(
                    text = "🤕",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        content = {
            Text(
                text = stringResource(R.string.headache_title),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            val severity = HeadacheSeverity.fromLevel(item.entry.severity)
            Text(
                text = stringResource(
                    R.string.headache_meta,
                    formatTimestampToTime(item.entry.startedAtMillis, userSettings),
                    headacheSeverityLabel(severity),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.caffeine_mg_compact, item.inferredCaffeineMg.toInt()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
        },
        shapes = segmentedListItemShapes(index, count),
        colors = ListItemDefaults.colors(
            containerColor = CaffeineSurfaceDefaults.groupedListContainerColor,
        ),
    )
}

@Composable
private fun ReportHeadacheSheet(
    userSettings: UserSettings,
    onSubmit: (startedAtMillis: Long, severity: Int, note: String) -> Unit,
) {
    val haptics = rememberAppHaptics()
    var startedAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var severity by remember { mutableStateOf(HeadacheSeverity.MODERATE) }
    var note by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "🤕", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.headache_report_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.headache_when),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { showTimePicker = true }) {
                Text(formatTimestampToDateTime(startedAtMillis, userSettings))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.headache_severity),
                style = MaterialTheme.typography.titleMedium,
            )
            val severities = HeadacheSeverity.entries
            Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                severities.forEachIndexed { index, option ->
                    ToggleButton(
                        checked = severity == option,
                        onCheckedChange = { if (it) { haptics.toggle(); severity = option } },
                        modifier = Modifier.weight(1f),
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            severities.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        Text(
                            text = headacheSeverityLabel(option),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.headache_note_label)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                haptics.confirm()
                onSubmit(startedAtMillis, severity.level, note)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(stringResource(R.string.headache_save))
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showTimePicker) {
        DateTimePickerDialog(
            currentTimestampMillis = startedAtMillis,
            settings = userSettings,
            onDateTimeSelected = {
                startedAtMillis = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun HeadacheDetailSheet(
    item: HomeTimelineItem.Headache,
    userSettings: UserSettings,
    onDelete: () -> Unit,
) {
    val severity = HeadacheSeverity.fromLevel(item.entry.severity)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "🤕", style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(
                    text = stringResource(R.string.headache_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = formatTimestampToDateTime(item.entry.startedAtMillis, userSettings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()

        HeadacheDetailRow(
            label = stringResource(R.string.headache_inferred_caffeine),
            value = stringResource(R.string.caffeine_mg, item.inferredCaffeineMg.toInt()),
            valueColor = MaterialTheme.colorScheme.error,
        )
        HeadacheDetailRow(
            label = stringResource(R.string.headache_severity),
            value = headacheSeverityLabel(severity),
        )
        if (item.entry.note.isNotBlank()) {
            HeadacheDetailRow(
                label = stringResource(R.string.headache_note_label),
                value = item.entry.note,
            )
        }

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.headache_delete))
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeadacheDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConsumptionHistoryListItem(
    entry: ConsumptionEntry,
    index: Int,
    count: Int,
    userSettings: UserSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            ExpressiveIconBadge(
                index = index,
                size = 44.dp,
            ) {
                DrinkIcon(
                    imageName = entry.imageName,
                    emoji = entry.emoji,
                    contentDescription = entry.drinkName,
                    modifier = Modifier.size(28.dp),
                    emojiSize = MaterialTheme.typography.titleLarge.fontSize,
                )
            }
        },
        content = {
            Text(
                text = entry.drinkName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            val meta = buildLoggedEntryMetaText(entry, userSettings)
            Text(
                text = if (entry.taken) {
                    meta
                } else {
                    stringResource(R.string.home_scheduled_meta, meta)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.taken) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.caffeine_mg_compact, entry.caffeineMg),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        },
        shapes = segmentedListItemShapes(index, count),
        colors = ListItemDefaults.colors(
            containerColor = CaffeineSurfaceDefaults.groupedListContainerColor,
        ),
    )
}

@Composable
private fun SleepForecastCard(
    caffeineAtBedtimeMg: Double,
    userSettings: UserSettings,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                caffeineAtBedtimeMg < userSettings.sleepThresholdMg ->
                    MaterialTheme.colorScheme.primaryContainer
                caffeineAtBedtimeMg < userSettings.sleepThresholdMg * 1.5 ->
                    MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    caffeineAtBedtimeMg < userSettings.sleepThresholdMg -> Icons.Default.CheckCircle
                    caffeineAtBedtimeMg < userSettings.sleepThresholdMg * 1.5 -> Icons.Default.Warning
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sleep_forecast_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = when {
                        caffeineAtBedtimeMg < userSettings.sleepThresholdMg ->
                            stringResource(R.string.sleep_forecast_safe, formatBedtime(userSettings))
                        caffeineAtBedtimeMg < userSettings.sleepThresholdMg * 1.5 ->
                            stringResource(R.string.sleep_forecast_may_affect, caffeineAtBedtimeMg.toInt())
                        else ->
                            stringResource(R.string.sleep_forecast_disruption, caffeineAtBedtimeMg.toInt())
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun WithdrawalForecastCard(
    caffeineAtWakeMg: Double,
    wakeTimeMillis: Long,
    userSettings: UserSettings,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val contentColor = MaterialTheme.colorScheme.onTertiaryContainer

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.withdrawal_forecast_short),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_dismiss),
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = stringResource(
                        R.string.withdrawal_forecast_warning,
                        formatTimestampToTime(wakeTimeMillis, userSettings),
                        caffeineAtWakeMg.toInt(),
                        userSettings.withdrawalThresholdMg,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.padding(start = 28.dp, top = 2.dp, bottom = 6.dp, end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ConsumptionLogDetailSheet(
    entry: ConsumptionEntry,
    detail: ConsumptionContributionDetail?,
    canRevealDetailContent: Boolean,
    userSettings: UserSettings,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMarkTaken: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    val presentedDetail = detail.takeIf { canRevealDetailContent }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveIconBadge(
                    index = entry.id,
                    size = 76.dp,
                ) {
                    DrinkIcon(
                        imageName = entry.imageName,
                        emoji = entry.emoji,
                        contentDescription = entry.drinkName,
                        modifier = Modifier.size(48.dp),
                        emojiSize = MaterialTheme.typography.headlineLarge.fontSize
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.drinkName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.home_started_meta, buildLoggedEntryMetaText(entry, userSettings)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = presentedDetail,
                        transitionSpec = {
                            fadeIn(
                                animationSpec = tween(
                                    durationMillis = DetailContentFadeInDurationMillis,
                                    delayMillis = DetailContentFadeInDelayMillis,
                                )
                            ) togetherWith fadeOut(
                                animationSpec = tween(durationMillis = DetailContentFadeOutDurationMillis)
                            )
                        },
                        label = "detail-sheet-summary"
                    ) { targetDetail ->
                        if (targetDetail == null) {
                            SkeletonSummaryLine()
                        } else {
                            Text(
                                text = stringResource(R.string.home_adds_now, formatPreciseMg(targetDetail.currentContributionMg)),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = presentedDetail,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = DetailContentFadeInDurationMillis,
                        delayMillis = DetailContentFadeInDelayMillis,
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = DetailContentFadeOutDurationMillis)
                )
            },
            label = "detail-sheet-body"
        ) { targetDetail ->
            if (targetDetail == null) {
                SkeletonDetailSheetBody()
            } else {
                ConsumptionLogDetailSheetBody(
                    entry = entry,
                    detail = targetDetail,
                    userSettings = userSettings,
                )
            }
        }

        if (!entry.taken) {
            Button(
                onClick = {
                    haptics.confirm()
                    onMarkTaken()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.home_action_mark_taken))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            SheetActionButton(
                modifier = Modifier.weight(1f),
                index = 0,
                count = 3,
                icon = Icons.Default.Edit,
                label = stringResource(R.string.home_action_edit),
                enabled = presentedDetail != null,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    haptics.navigation()
                    onEdit()
                }
            )
            SheetActionButton(
                modifier = Modifier.weight(1f),
                index = 1,
                count = 3,
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.home_action_duplicate),
                onClick = {
                    haptics.navigation()
                    onDuplicate()
                }
            )
            SheetActionButton(
                modifier = Modifier.weight(1f),
                index = 2,
                count = 3,
                icon = Icons.Default.Delete,
                label = stringResource(R.string.home_action_delete),
                tint = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                onClick = {
                    haptics.navigation()
                    onDelete()
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ConsumptionLogDetailSheetBody(
    entry: ConsumptionEntry,
    detail: ConsumptionContributionDetail,
    userSettings: UserSettings,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            color = CaffeineSurfaceDefaults.detailPanelContainerColor,
            shape = MaterialTheme.shapes.large
        ) {
            ConsumptionContributionChart(
                detail = detail,
                userSettings = userSettings,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.home_drink_contribution_title),
                style = MaterialTheme.typography.titleMedium
            )
            ContributionStatRow(
                label = stringResource(R.string.home_at_peak, formatLoggedTime(detail.peakTimestampMillis, userSettings)),
                value = formatPreciseMg(detail.peakContributionMg)
            )
            ContributionStatRow(
                label = stringResource(R.string.home_now),
                value = formatPreciseMg(detail.currentContributionMg)
            )
            HorizontalDivider()
            ContributionStatRow(
                label = stringResource(R.string.home_in_total_over_time),
                value = formatPreciseMg(detail.totalContributionMg)
            )
        }
    }
}

@Composable
private fun SkeletonSummaryLine() {
    SkeletonBlock(
        modifier = Modifier
            .fillMaxWidth(0.58f)
            .height(22.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SkeletonDetailSheetBody() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            color = CaffeineSurfaceDefaults.detailPanelContainerColor,
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 16.dp),
                    shape = MaterialTheme.shapes.medium
                )
                SkeletonBlock(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, end = 4.dp, bottom = 10.dp)
                        .fillMaxWidth()
                        .height(2.dp),
                    shape = RoundedCornerShape(999.dp)
                )
                SkeletonBlock(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 16.dp)
                        .width(2.dp)
                        .height(132.dp),
                    shape = RoundedCornerShape(999.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(24.dp),
                shape = RoundedCornerShape(12.dp)
            )
            SkeletonStatRow()
            SkeletonStatRow()
            SkeletonStatRow()
        }
    }
}

@Composable
private fun SkeletonStatRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBlock(
            modifier = Modifier
                .weight(1f)
                .padding(end = 56.dp)
                .height(18.dp),
            shape = RoundedCornerShape(10.dp)
        )
        SkeletonBlock(
            modifier = Modifier
                .width(72.dp)
                .height(18.dp),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    shape: Shape,
) {
    Box(
        modifier = modifier.shimmerEffect(shape)
    )
}

@Composable
private fun ContributionStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SheetActionButton(
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: Color? = null,
    containerColor: Color? = null,
    onClick: () -> Unit
) {
    val contentTint = tint ?: MaterialTheme.colorScheme.onSurface
    val resolvedContainerColor = containerColor ?: MaterialTheme.colorScheme.surfaceVariant
    val buttonShape = when (index) {
        0 -> RoundedCornerShape(
            topStart = 36.dp,
            bottomStart = 36.dp,
            topEnd = 4.dp,
            bottomEnd = 4.dp,
        )
        count - 1 -> RoundedCornerShape(
            topStart = 4.dp,
            bottomStart = 4.dp,
            topEnd = 36.dp,
            bottomEnd = 36.dp,
        )
        else -> RoundedCornerShape(4.dp)
    }

    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(72.dp),
        shape = buttonShape,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = resolvedContainerColor,
            contentColor = contentTint,
            disabledContainerColor = resolvedContainerColor.copy(alpha = 0.5f),
            disabledContentColor = contentTint.copy(alpha = 0.38f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentTint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditConsumptionEntrySheet(
    entry: ConsumptionEntry,
    viewModel: CaffeineViewModel,
    userSettings: UserSettings,
    onBack: () -> Unit,
    onSave: (Int, DrinkUnit, Long, Int) -> Unit
) {
    val availableUnits by produceState<List<DrinkUnit>?>(initialValue = null, key1 = entry.id, key2 = entry.presetItemId) {
        value = viewModel.getUnitsForPresetItemId(entry.presetItemId)
    }
    var quantity by remember(entry.id) {
        mutableStateOf(entry.quantity.coerceAtLeast(1))
    }
    var startedAtMillis by remember(entry.id) {
        mutableStateOf(entry.startedAtMillis)
    }
    var durationMinutes by remember(entry.id) {
        mutableIntStateOf(entry.normalizedDurationMinutes)
    }
    val fallbackUnit = remember(entry) {
        if (entry.unitKey.isBlank()) {
            null
        } else {
            DrinkUnit(
                drinkId = 0,
                unitKey = entry.unitKey,
                caffeineMg = entry.unitCaffeineMg,
                milliliters = null,
                grams = null,
                isDefault = true,
            )
        }
    }
    val initialUnit = remember(availableUnits, entry.unitKey, entry.unitCaffeineMg, fallbackUnit) {
        val resolvedUnits = availableUnits.orEmpty()
        if (resolvedUnits.isEmpty()) {
            fallbackUnit
        } else {
            findMatchingUnit(resolvedUnits, entry.unitKey, entry.unitCaffeineMg)
        }
    }
    val displayedUnits = remember(availableUnits, fallbackUnit) {
        val resolvedUnits = availableUnits.orEmpty()
        if (resolvedUnits.isEmpty()) {
            listOfNotNull(fallbackUnit)
        } else {
            resolvedUnits
        }
    }
    var selectedUnitKey by remember(entry.id, availableUnits) {
        mutableStateOf(initialUnit?.unitKey)
    }
    val selectedUnit = remember(displayedUnits, selectedUnitKey, initialUnit) {
        displayedUnits.firstOrNull { it.unitKey == selectedUnitKey } ?: initialUnit
    }
    val totalCaffeineMg = remember(quantity, selectedUnit) {
        selectedUnit?.let { calculateServingTotalCaffeine(quantity, it.caffeineMg) } ?: entry.caffeineMg
    }
    val isSaveEnabled = selectedUnit != null
    val haptics = rememberAppHaptics()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = {
                haptics.navigation()
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Text(
                text = stringResource(R.string.home_edit_drink, entry.drinkName),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(
                enabled = isSaveEnabled,
                onClick = {
                    selectedUnit?.let { unit ->
                        haptics.navigation()
                        onSave(quantity, unit, startedAtMillis, durationMinutes)
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        }

        if (availableUnits == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                ContainedLoadingIndicator()
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.home_serving),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ServingQuantityStepper(
                    quantity = quantity,
                    onDecrement = { quantity = (quantity - 1).coerceAtLeast(1) },
                    onIncrement = { quantity += 1 },
                    onQuantitySet = { quantity = it },
                )
                RollingNumberText(
                    text = stringResource(R.string.caffeine_mg, totalCaffeineMg),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    labelPrefix = "edit_entry_total",
                )
                if (displayedUnits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_serving_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    ServingUnitSelector(
                        units = displayedUnits,
                        selectedUnit = selectedUnit,
                        onUnitSelected = { selectedUnitKey = it.unitKey },
                    )
                }
            }
        }

        ConsumptionTimingSection(
            startedAtMillis = startedAtMillis,
            durationMinutes = durationMinutes,
            settings = userSettings,
            onStartedAtChange = { startedAtMillis = it },
            onDurationChange = { durationMinutes = it },
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun formatBedtime(settings: UserSettings): String {
    return formatTimeOfDay(settings.sleepTimeHour, settings.sleepTimeMinute, settings)
}

private fun formatTimelineHeaderText(
    date: LocalDate,
    settings: UserSettings,
    referenceTimeMillis: Long,
): String {
    return formatConsumptionDateHeader(
        date = date,
        settings = settings,
        referenceTimeMillis = referenceTimeMillis
    )
}

private fun formatLoggedTime(
    timestampMillis: Long,
    settings: UserSettings
): String {
    return formatTimestampToTime(timestampMillis, settings)
}

private fun buildLoggedEntryMetaText(
    entry: ConsumptionEntry,
    settings: UserSettings,
): String {
    return buildString {
        append(formatLoggedTime(entry.startedAtMillis, settings))
        append(" • ")
        append(formatLoggedServing(entry))
        append(" • ")
        append(formatDurationMinutes(entry.normalizedDurationMinutes))
    }
}

private fun formatLoggedServing(entry: ConsumptionEntry): String {
    return if (entry.unitKey.isBlank()) {
        "${entry.caffeineMg}mg"
    } else {
        formatServingSummary(entry.quantity, entry.unitKey)
    }
}

private fun formatPreciseMg(value: Double): String {
    return String.format(Locale.getDefault(), "%.1f mg", value)
}
