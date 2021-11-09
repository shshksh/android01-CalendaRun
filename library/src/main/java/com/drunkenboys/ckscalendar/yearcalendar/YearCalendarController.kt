package com.drunkenboys.ckscalendar.yearcalendar

import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.drunkenboys.ckscalendar.data.CalendarDate
import com.drunkenboys.ckscalendar.data.CalendarSet
import com.drunkenboys.ckscalendar.data.DayType
import com.drunkenboys.ckscalendar.utils.TimeUtils
import java.time.DayOfWeek

class YearCalendarController {

    fun dayOfWeekConstraints(weekIds: List<String>) = ConstraintSet {
        val sunday = createRefFor(weekIds[0])
        val monday = createRefFor(weekIds[1])
        val tuesday = createRefFor(weekIds[2])
        val wednesday = createRefFor(weekIds[3])
        val thursday = createRefFor(weekIds[4])
        val friday = createRefFor(weekIds[5])
        val saturday = createRefFor(weekIds[6])

        constrain(sunday) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(monday.start)
            width = Dimension.fillToConstraints
        }

        constrain(monday) {
            top.linkTo(parent.top)
            start.linkTo(sunday.end)
            end.linkTo(tuesday.start)
            width = Dimension.fillToConstraints
        }

        constrain(tuesday) {
            top.linkTo(parent.top)
            start.linkTo(monday.end)
            end.linkTo(wednesday.start)
            width = Dimension.fillToConstraints
        }

        constrain(wednesday) {
            top.linkTo(parent.top)
            start.linkTo(tuesday.end)
            end.linkTo(thursday.start)
            width = Dimension.fillToConstraints
        }

        constrain(thursday) {
            top.linkTo(parent.top)
            start.linkTo(wednesday.end)
            end.linkTo(friday.start)
            width = Dimension.fillToConstraints
        }

        constrain(friday) {
            top.linkTo(parent.top)
            start.linkTo(thursday.end)
            end.linkTo(saturday.start)
            width = Dimension.fillToConstraints
        }

        constrain(saturday) {
            top.linkTo(parent.top)
            start.linkTo(friday.end)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    }

    fun calendarSetToCalendarDates(month: CalendarSet): List<List<CalendarDate>> {
        // n주
        val weekList = mutableListOf<MutableList<CalendarDate>>()
        var oneDay = month.startDate
        var paddingPrev = month.startDate
        var paddingNext = month.endDate

        // 앞쪽 패딩
        if (paddingPrev.dayOfWeek != DayOfWeek.SUNDAY) weekList.add(mutableListOf())
        while (paddingPrev.dayOfWeek != DayOfWeek.SUNDAY) {
            paddingPrev = paddingPrev.minusDays(1)
            weekList.last().add(CalendarDate(paddingPrev, DayType.PADDING))
        }

        // n주일 추가
        repeat(month.startDate.lengthOfMonth()) {
            // 일요일에는 1주일 갱신
            if (oneDay.dayOfWeek == DayOfWeek.SUNDAY) weekList.add(mutableListOf())

            // 1주일 추가
            weekList.last().add(CalendarDate(oneDay, TimeUtils.parseDayWeekToDayType(oneDay.dayOfWeek)))

            oneDay = oneDay.plusDays(1L)
        }

        // 뒤쪽 패딩
        while (paddingNext.dayOfWeek != DayOfWeek.SATURDAY) {
            paddingNext = paddingNext.plusDays(1)
            weekList.last().add(CalendarDate(paddingNext, DayType.PADDING))
        }

        return weekList
    }

    fun isFirstWeek(week: List<CalendarDate>, monthId: Int) = week.any { day ->
        day.date.dayOfMonth == 1 && monthId == day.date.monthValue
    }
}