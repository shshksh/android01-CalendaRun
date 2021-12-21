package com.drunkenboys.calendarun.ui.saveschedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.drunkenboys.calendarun.KEY_CALENDAR_ID
import com.drunkenboys.calendarun.KEY_SCHEDULE_ID
import com.drunkenboys.calendarun.data.calendar.entity.Calendar
import com.drunkenboys.calendarun.data.calendar.local.CalendarLocalDataSource
import com.drunkenboys.calendarun.data.calendar.local.FakeCalendarLocalDataSource
import com.drunkenboys.calendarun.data.schedule.entity.Schedule
import com.drunkenboys.calendarun.data.schedule.local.FakeScheduleLocalDataSource
import com.drunkenboys.calendarun.data.schedule.local.ScheduleLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SaveScheduleViewModelTest {

    private lateinit var scheduleDataSource: ScheduleLocalDataSource
    private lateinit var calendarDataSource: CalendarLocalDataSource

    private lateinit var viewModel: SaveScheduleViewModel

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        scheduleDataSource = FakeScheduleLocalDataSource()
        calendarDataSource = FakeCalendarLocalDataSource()
        viewModel = createSaveScheduleViewModel(0)
    }

    private fun createSaveScheduleViewModel(scheduleId: Long) = SaveScheduleViewModel(
        SavedStateHandle(mapOf(KEY_CALENDAR_ID to 0L, KEY_SCHEDULE_ID to scheduleId)),
        calendarDataSource,
        scheduleDataSource
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `뷰모델_초기화_테스트`() = testScope.runBlockingTest {
        val calendarName = "test calendar"
        calendarDataSource.insertCalendar(Calendar(0, calendarName, LocalDate.now(), LocalDate.now()))
        viewModel = createSaveScheduleViewModel(0)

        assertEquals(calendarName, viewModel.calendarName.value)
    }

    @Test
    fun `일정_수정_시_뷰모델_초기화_테스트`() = testScope.runBlockingTest {
        val startDate = LocalDateTime.now()
        val endDate = LocalDateTime.now()
        scheduleDataSource.insertSchedule(Schedule(1, 0, "test", startDate, endDate, Schedule.NotificationType.A_HOUR_AGO, "memo", 0))

        viewModel = createSaveScheduleViewModel(1)

        assertEquals("test", viewModel.title.value)
        assertEquals(startDate, viewModel.startDate.value)
        assertEquals(endDate, viewModel.endDate.value)
        assertEquals(Schedule.NotificationType.A_HOUR_AGO, viewModel.notificationType.value)
        assertEquals("memo", viewModel.memo.value)
        assertEquals(0, viewModel.tagColor.value)
    }

    @Test
    fun `제목_미입력_시_저장_테스트`() = testScope.runBlockingTest {
        lateinit var testFlow: StateFlow<Unit>
        viewModel.viewModelScope.launch {
            testFlow = viewModel.blankTitleEvent.stateIn(this)
        }

        viewModel.saveSchedule()

        assertEquals(Unit, testFlow.value)
    }

    @Test
    fun `정상_입력_시_저장_테스트`() = testScope.runBlockingTest {
        val title = "test title"
        val memo = "test memo"
        viewModel.title.value = title
        viewModel.memo.value = memo

        lateinit var testFlow: StateFlow<Pair<Schedule, String>>
        viewModel.viewModelScope.launch {
            testFlow = viewModel.saveScheduleEvent.stateIn(this)
        }

        viewModel.saveSchedule()

        assertEquals(title, testFlow.value.first.name)
        assertEquals(memo, testFlow.value.first.memo)
    }

    @Test
    fun `일정_삭제_테스트`() = testScope.runBlockingTest {
        val startDate = LocalDateTime.now()
        val endDate = LocalDateTime.now()
        val schedule = Schedule(1, 0, "test", startDate, endDate, Schedule.NotificationType.A_HOUR_AGO, "memo", 0)
        scheduleDataSource.insertSchedule(schedule)

        viewModel = createSaveScheduleViewModel(1)

        lateinit var testFlow: StateFlow<Pair<Schedule, String>>
        viewModel.viewModelScope.launch {
            testFlow = viewModel.deleteScheduleEvent.stateIn(this)
        }

        viewModel.deleteSchedule()

        assertEquals(schedule, testFlow.value.first)
    }
}
