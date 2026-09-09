package dev.charter.feature.anime.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeBangumiRepository
import dev.charter.feature.anime.FakeCollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bangumiRepository = FakeBangumiRepository()
    private val collectionRepository = FakeCollectionRepository()

    private fun viewModel() = TimelineViewModel(bangumiRepository, collectionRepository)

    @Test
    fun `calendar becomes Content with seven days`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.calendarDays.value = TestBangumi.calendar()
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(TimelineUiState.Content::class.java)
                assertThat((content as TimelineUiState.Content).days).hasSize(7)
            }
        }

    @Test
    fun `only-following filter keeps collected items`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.calendarDays.value = TestBangumi.calendar(itemsPerDay = 2)
            collectionRepository.entries.value = listOf(TestBangumi.collectionEntry(bangumiId = 11))
            val viewModel = viewModel()

            viewModel.toggleOnlyFollowing()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem() as TimelineUiState.Content
                val total = content.days.sumOf { it.items.size }
                assertThat(total).isEqualTo(1)
                assertThat(content.onlyFollowing).isTrue()
            }
        }

    @Test
    fun `init refreshes the calendar`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel()
            advanceUntilIdle()
            assertThat(bangumiRepository.refreshCalendarCalls).isAtLeast(1)
        }

    @Test
    fun `selectDay moves the selection`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.calendarDays.value = TestBangumi.calendar()
            val viewModel = viewModel()

            viewModel.selectDay(5)

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem() as TimelineUiState.Content
                assertThat(content.selectedWeekday).isEqualTo(5)
            }
        }
}
