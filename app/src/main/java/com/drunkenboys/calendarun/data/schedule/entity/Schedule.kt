package com.drunkenboys.calendarun.data.schedule.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.drunkenboys.calendarun.data.calendar.entity.Calendar
import java.util.*

@Entity(
    tableName = "Schedule",
    foreignKeys = [
        ForeignKey(
            entity = Calendar::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 1,
    val calendarId: Int,
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val notification: Date,
    val memo: String,
    // TODO <Library>.ScheduleColorType
    val color: Int
)