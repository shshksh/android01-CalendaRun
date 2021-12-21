package com.drunkenboys.calendarun.ui.managecalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drunkenboys.calendarun.data.calendar.local.CalendarLocalDataSource
import com.drunkenboys.calendarun.ui.managecalendar.model.CalendarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageCalendarViewModel @Inject constructor(
    private val calendarLocalDataSource: CalendarLocalDataSource
) : ViewModel() {

    val calendarItemList = calendarLocalDataSource.fetchCustomCalendar()
        .map { calendarList ->
            calendarList.map { calendar -> CalendarItem.from(calendar, ::emitCalendarClickEvent) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _openDeleteDialogEvent = MutableSharedFlow<Int>()
    val openDeleteDialogEvent: SharedFlow<Int> = _openDeleteDialogEvent

    private val _calendarClickEvent = MutableSharedFlow<Long>()
    val calendarClickEvent: SharedFlow<Long> = _calendarClickEvent

    fun deleteCheckedCalendar() {
        viewModelScope.launch {
            calendarItemList.value
                .filter { calendarItem -> calendarItem.check }
                .forEach { calendarItem -> calendarLocalDataSource.deleteCalendar(calendarItem.toCalendar()) }
        }
    }

    private fun emitCalendarClickEvent(calendarId: Long) {
        viewModelScope.launch {
            _calendarClickEvent.emit(calendarId)
        }
    }

    fun emitOpenDeleteDialogEvent(id: Int) {
        viewModelScope.launch {
            _openDeleteDialogEvent.emit(id)
        }
    }
}
