package com.drunkenboys.calendarun.data.schedule.local

import com.drunkenboys.calendarun.data.schedule.entity.Schedule
import com.drunkenboys.calendarun.util.defaultZoneOffset

class FakeScheduleLocalDataSource : ScheduleLocalDataSource {

    private val database = mutableListOf<Schedule>()

    override suspend fun insertSchedule(schedule: Schedule): Long {
        database.add(schedule)
        return database.size.toLong()
    }

    override suspend fun fetchAllSchedule(): List<Schedule> {
        return database
    }

    override suspend fun fetchSchedule(id: Long): Schedule {
        return database.find { it.id == id } ?: throw IllegalArgumentException()
    }

    override suspend fun fetchCalendarSchedules(calendarId: Long): List<Schedule> {
        return database.filter { it.calendarId == calendarId }
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        val targetIndex = database.indexOfFirst { it.id == schedule.id }
        database.removeAt(targetIndex)
        database.add(targetIndex, schedule)
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        database.remove(schedule)
    }

    override suspend fun fetchMatchedScheduleAfter(word: String, time: Long): List<Schedule> {
        return database.filter { it.startDate.toEpochSecond(defaultZoneOffset) > time && word in it.name }
    }

    override suspend fun fetchMatchedScheduleBefore(word: String, time: Long): List<Schedule> {
        return database.filter { it.startDate.toEpochSecond(defaultZoneOffset) < time && word in it.name }
    }
}
