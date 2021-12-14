package com.drunkenboys.calendarun.ui.savecalendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drunkenboys.calendarun.KEY_CALENDAR_ID
import com.drunkenboys.calendarun.data.calendar.entity.Calendar
import com.drunkenboys.calendarun.data.calendar.local.CalendarLocalDataSource
import com.drunkenboys.calendarun.data.slice.entity.Slice
import com.drunkenboys.calendarun.data.slice.local.SliceLocalDataSource
import com.drunkenboys.calendarun.ui.savecalendar.model.SliceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaveCalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calendarLocalDataSource: CalendarLocalDataSource,
    private val sliceLocalDataSource: SliceLocalDataSource
) : ViewModel() {

    private val calendarId = savedStateHandle[KEY_CALENDAR_ID] ?: 0L

    private val isUpdate = calendarId > 0

    private val _sliceItemList: MutableStateFlow<List<SliceItem>> = MutableStateFlow(emptyList())
    val sliceItemList: StateFlow<List<SliceItem>> = _sliceItemList

    val calendarName = MutableStateFlow("")

    val isDefaultCalendar = MutableStateFlow(false)

    private val _saveCalendarEvent = MutableSharedFlow<Boolean>()
    val saveCalendarEvent: SharedFlow<Boolean> = _saveCalendarEvent

    private val _blankTitleEvent = MutableSharedFlow<Unit>()
    val blankTitleEvent: SharedFlow<Unit> = _blankTitleEvent

    private val deleteSliceIdList = mutableListOf<Long>()

    init {
        restoreCalendarData()
        if (!isUpdate) _sliceItemList.value = listOf(SliceItem())
    }

    private fun restoreCalendarData() {
        if (!isUpdate) return

        viewModelScope.launch {
            val calendar = calendarLocalDataSource.fetchCalendar(calendarId)

            calendarName.value = calendar.name
            _sliceItemList.value = sliceLocalDataSource.fetchCalendarSliceList(calendarId)
                .firstOrNull()
                ?.map { SliceItem.from(it) }
                ?: return@launch
            isDefaultCalendar.value = sliceItemList.value.isEmpty()
        }
    }

    fun emitSaveCalendar() {
        viewModelScope.launch {
            _saveCalendarEvent.emit(saveCalendarInfo())
        }
    }

    fun addSlice() {
        _sliceItemList.value += SliceItem()
    }

    private fun deleteSliceList(calendarId: Long) {
        viewModelScope.launch {
            sliceLocalDataSource.deleteSliceList(calendarId)
        }
    }

    fun deleteCheckedSlice() {
        deleteSliceIdList += sliceItemList.value
            .filter { sliceItem -> sliceItem.check }
            .map { sliceItem -> sliceItem.id }

        _sliceItemList.value = sliceItemList.value.filter { sliceItem -> !sliceItem.check }
    }

    private fun emitBlankTitleEvent() {
        viewModelScope.launch {
            _blankTitleEvent.emit(Unit)
        }
    }

    private suspend fun saveCalendarInfo(): Boolean {
        val useDefaultCalendar = isDefaultCalendar.value
        val calendarName = calendarName.value
        val sliceList = _sliceItemList.value
        var canSave = true

        if (calendarName.isBlank()) {
            emitBlankTitleEvent()
            canSave = false
        }

        if (useDefaultCalendar && canSave) {
            saveCalendar(calendarId, calendarName)
            deleteSliceList(calendarId)
            return true
        }

        var calendarStartDate = sliceList.getOrNull(0)?.startDate?.value ?: LocalDate.now()
        var calendarEndDate = sliceList.getOrNull(0)?.endDate?.value ?: LocalDate.now()

        sliceList.forEach { item ->
            val name = item.name.value
            val startDate = item.startDate.value
            val endDate = item.endDate.value

            if (name.isBlank()) {
                emitBlankSliceNameEvent(item)
                canSave = false
            }
            if (startDate == null || endDate == null) {
                emitBlankSliceStartDateEvent(item)
                emitBlankSliceEndDateEvent(item)
                canSave = false
            }

            startDate?.let {
                if (calendarStartDate.isAfter(it)) {
                    calendarStartDate = it
                }
            }

            endDate?.let {
                if (calendarEndDate.isBefore(it)) {
                    calendarEndDate = it
                }
            }
        }

        if (!canSave) return false

        val newCalendarId = saveCalendar(calendarId, calendarName, calendarStartDate, calendarEndDate)

        sliceList.forEach { item ->
            saveSlice(item, newCalendarId)
        }

        deleteSlice()

        return true
    }

    private suspend fun saveCalendar(
        id: Long,
        name: String,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now()
    ): Long {
        var calendarId = id

        calendarId = withContext(viewModelScope.coroutineContext) {
            val newCalendar = Calendar(
                id = id,
                name = name,
                startDate = startDate,
                endDate = endDate
            )

            if (id != 0L) {
                calendarLocalDataSource.updateCalendar(newCalendar)
            } else {
                calendarId = calendarLocalDataSource.insertCalendar(newCalendar)
            }
            calendarId
        }

        return calendarId
    }

    private fun saveSlice(item: SliceItem, newCalendarId: Long) {
        val newSlice = Slice(
            id = item.id,
            calendarId = newCalendarId,
            name = item.name.value,
            startDate = item.startDate.value ?: return,
            endDate = item.endDate.value ?: return
        )

        viewModelScope.launch {
            if (item.id != 0L) {
                sliceLocalDataSource.updateSlice(newSlice)
            } else {
                sliceLocalDataSource.insertSlice(newSlice)
            }
        }
    }

    private fun deleteSlice() {
        viewModelScope.launch {
            deleteSliceIdList.forEach { id ->
                sliceLocalDataSource.deleteSliceById(id)
            }
        }
    }

    private fun emitBlankSliceNameEvent(item: SliceItem) {
        viewModelScope.launch {
            item.isNameBlank.emit(Unit)
        }
    }

    private fun emitBlankSliceStartDateEvent(item: SliceItem) {
        viewModelScope.launch {
            item.isStartDateBlank.emit(Unit)
        }
    }

    private fun emitBlankSliceEndDateEvent(item: SliceItem) {
        viewModelScope.launch {
            item.isEndDateBlank.emit(Unit)
        }
    }
}
