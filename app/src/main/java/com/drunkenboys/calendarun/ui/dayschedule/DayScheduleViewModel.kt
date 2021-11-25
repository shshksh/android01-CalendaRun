package com.drunkenboys.calendarun.ui.dayschedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drunkenboys.calendarun.KEY_CALENDAR_ID
import com.drunkenboys.calendarun.data.schedule.entity.Schedule
import com.drunkenboys.calendarun.data.schedule.local.ScheduleLocalDataSource
import com.drunkenboys.calendarun.ui.searchschedule.model.ScheduleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DayScheduleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scheduleDataSource: ScheduleLocalDataSource
) : ViewModel() {

    private val calendarId = savedStateHandle[KEY_CALENDAR_ID] ?: 0L

    private val _dateString = MutableStateFlow("")
    val dateString: StateFlow<String> = _dateString

    private val _listItem = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val listItem: StateFlow<List<ScheduleItem>> = _listItem

    private val _scheduleClickEvent = MutableSharedFlow<Schedule>()
    val scheduleClickEvent: SharedFlow<Schedule> = _scheduleClickEvent

    fun fetchScheduleList(localDate: LocalDate) {
        viewModelScope.launch {
            _dateString.emit(localDate.format(DateTimeFormatter.ofPattern("M월 d일")))

            scheduleDataSource.fetchCalendarSchedules(calendarId).firstOrNull()
                ?.filter { localDate in it.startDate.toLocalDate()..it.endDate.toLocalDate() }
                ?.map { schedule ->
                    ScheduleItem(schedule) { emitScheduleClickEvent(schedule) }
                }
                ?.sortedBy { dateScheduleItem -> dateScheduleItem.schedule.startDate }
                ?.let { _listItem.emit(it) }
        }
    }

    private fun emitScheduleClickEvent(schedule: Schedule) {
        viewModelScope.launch {
            _scheduleClickEvent.emit(schedule)
        }
    }
}
