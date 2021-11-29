package com.drunkenboys.calendarun.ui.searchschedule

import com.drunkenboys.calendarun.data.schedule.entity.Schedule
import com.drunkenboys.calendarun.data.schedule.local.FakeScheduleLocalDataSource
import com.drunkenboys.calendarun.data.schedule.local.ScheduleLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SearchScheduleViewModelTest {

    private lateinit var dataSource: ScheduleLocalDataSource

    private lateinit var viewModel: SearchScheduleViewModel

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataSource = FakeScheduleLocalDataSource()
        viewModel = SearchScheduleViewModel(dataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `이후_일정이_있을_때_초기_검색_테스트`() = testScope.runBlockingTest {
        val localDateTime = LocalDateTime.now()
        initDataSource(dateList = arrayOf(localDateTime.minusDays(1), localDateTime.plusDays(1)))

        viewModel.searchSchedule()
        advanceTimeBy(500)
        val result = viewModel.listItem.value

        if (result.isNullOrEmpty()) {
            fail("LiveData's value is null or empty")
            return@runBlockingTest
        }
        assertEquals(2, result.size)
    }

    @Test
    fun `이후_일정이_없을_때_초기_검색_테스트`() = testScope.runBlockingTest {
        val localDateTime = LocalDateTime.now()
        initDataSource(dateList = arrayOf(localDateTime.minusDays(1), localDateTime.minusDays(1)))

        viewModel.searchSchedule()
        advanceTimeBy(500)
        val result = viewModel.listItem.value

        assertEquals(3, result.size)
    }

    @Test
    fun `검색_기능_테스트`() = testScope.runBlockingTest {
        val date = LocalDateTime.now().plusDays(1)
        initDataSource(name = "foo", dateList = arrayOf(date, date, date))
        initDataSource(name = "bar", dateList = arrayOf(date, date, date, date))
        initDataSource(name = "quz", dateList = arrayOf(date, date))

        viewModel.searchSchedule("bar")
        advanceTimeBy(500)
        val result = viewModel.listItem.value

        assertEquals(5, result.size)
    }

    private suspend fun initDataSource(name: String = "name", vararg dateList: LocalDateTime) {
        dateList.forEachIndexed { index, date ->
            dataSource.insertSchedule(
                Schedule(
                    id = index.toLong(),
                    calendarId = 1,
                    name = "$name$index",
                    startDate = date,
                    endDate = LocalDateTime.now(),
                    notificationType = Schedule.NotificationType.NONE,
                    memo = "memo$index",
                    color = 0
                )
            )
        }
    }
}
