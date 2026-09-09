package dev.charter.feature.anime.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.async.SingleFlight
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.onFailure
import dev.charter.core.common.result.onSuccess
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.CollectionRepository
import dev.charter.core.model.CalendarDay
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        private val bangumiRepository: BangumiRepository,
        private val collectionRepository: CollectionRepository,
    ) : ViewModel() {
        private val selectedWeekday = MutableStateFlow(CalendarDay.todayWeekday())
        private val onlyFollowing = MutableStateFlow(false)
        private val isRefreshing = MutableStateFlow(false)
        private val loadError = MutableStateFlow<AppError?>(null)
        private val refreshFlight = SingleFlight()

        val uiState: StateFlow<TimelineUiState> =
            combine(
                bangumiRepository.observeCalendar(),
                collectionRepository.observeAll(),
                selectedWeekday,
                onlyFollowing,
                isRefreshing,
            ) { days, collection, selected, following, refreshing ->
                val followed = collection.map { it.bangumiId }.toSet()
                val filtered =
                    if (following) {
                        days.map { day -> day.copy(items = day.items.filter { it.id in followed }) }
                    } else {
                        days
                    }
                Triple(filtered to selected, following, refreshing)
            }.combine(loadError) { (daysAndSelected, following, refreshing), error ->
                val (days, selected) = daysAndSelected
                when {
                    days.any { it.items.isNotEmpty() } ->
                        TimelineUiState.Content(
                            days = days.toImmutableList(),
                            selectedWeekday = selected,
                            onlyFollowing = following,
                            isRefreshing = refreshing,
                        )
                    error != null && !refreshing -> TimelineUiState.Error(error.userMessage())
                    refreshing -> TimelineUiState.Loading
                    else ->
                        TimelineUiState.Empty(
                            selectedWeekday = selected,
                            onlyFollowing = following,
                        )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                initialValue = TimelineUiState.Loading,
            )

        init {
            refresh()
        }

        fun refresh() {
            isRefreshing.value = true
            viewModelScope.launch {
                refreshFlight
                    .run { bangumiRepository.refreshCalendar() }
                    .onSuccess { loadError.value = null }
                    .onFailure { loadError.value = it }
                isRefreshing.value = false
            }
        }

        fun selectDay(weekday: Int) {
            selectedWeekday.value = weekday
        }

        fun toggleOnlyFollowing() {
            onlyFollowing.value = !onlyFollowing.value
        }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
