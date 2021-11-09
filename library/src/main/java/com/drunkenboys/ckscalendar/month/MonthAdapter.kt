package com.drunkenboys.ckscalendar.month

import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.drunkenboys.ckscalendar.data.CalendarDate
import com.drunkenboys.ckscalendar.data.CalendarScheduleObject
import com.drunkenboys.ckscalendar.data.DayType
import com.drunkenboys.ckscalendar.databinding.ItemMonthCellBinding
import com.drunkenboys.ckscalendar.listener.OnDayClickListener
import com.drunkenboys.ckscalendar.listener.OnDaySecondClickListener
import com.drunkenboys.ckscalendar.utils.context
import com.drunkenboys.ckscalendar.utils.dp2px

class MonthAdapter(val onDaySelectStateListener: OnDaySelectStateListener) : ListAdapter<CalendarDate, MonthAdapter.Holder>(diffUtil) {

    private val schedules = mutableListOf<CalendarScheduleObject>()

    var selectedPosition = -1
    var currentPagePosition = -1

    var onDateClickListener: OnDayClickListener? = null
    var onDateSecondClickListener: OnDaySecondClickListener? = null

    private val lineIndex = HashMap<String, Int>()

    fun setItems(
        list: List<CalendarDate>,
        schedules: List<CalendarScheduleObject>,
        currentPagePosition: Int
    ) {
        list.forEachIndexed { index, calendarDate ->
            if (calendarDate.isSelected) {
                selectedPosition = index
                return@forEachIndexed
            }
        }
        this.lineIndex.clear()
        this.currentPagePosition = currentPagePosition
        this.schedules.clear()
        this.schedules.addAll(schedules)
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val calculateHeight = parent.height / CALENDAR_COLUMN_SIZE

        return Holder(ItemMonthCellBinding.inflate(LayoutInflater.from(parent.context), parent, false), calculateHeight)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class Holder(private val binding: ItemMonthCellBinding, private val calculateHeight: Int) :
        RecyclerView.ViewHolder(binding.root) {

        private val weekDayColor = Color.BLACK
        private val holidayColor = Color.RED
        private val saturdayColor = Color.BLUE

        init {
            itemView.setOnClickListener {
                if (adapterPosition != -1 && currentList[adapterPosition].dayType != DayType.PADDING) {
                    notifyClickEventType()
                    notifyChangedSelectPosition(adapterPosition)
                    onDaySelectStateListener.onDaySelectChange(currentPagePosition, selectedPosition)
                }
            }
            binding.layoutMonthCell.layoutParams.height = calculateHeight
        }

        fun bind(item: CalendarDate) {
            binding.layoutMonthCell.isSelected = item.isSelected

            binding.tvMonthDay.text = ""
            if (item.dayType != DayType.PADDING) {
                binding.tvMonthDay.text = item.date.dayOfMonth.toString()
            }
            val textColor = when (item.dayType) {
                DayType.HOLIDAY, DayType.SUNDAY -> holidayColor
                DayType.SATURDAY -> saturdayColor
                else -> weekDayColor
            }
            binding.tvMonthDay.setTextColor(textColor)

            binding.layoutMonthSchedule.removeAllViews()
            val scheduleContainer = makePaddingScheduleList(item, schedules)
            val hasAnySchedule = scheduleContainer.any { it != null }
            if (hasAnySchedule) {
                scheduleContainer.map { it ?: makeDefaultScheduleTextView() }
                    .forEach {
                        binding.layoutMonthSchedule.addView(it)
                    }
            }
        }

        // make sorted schedule list with white padding
        private fun makePaddingScheduleList(item: CalendarDate, schedules: List<CalendarScheduleObject>): Array<TextView?> {
            val filteredScheduleList = schedules.filter {
                item.dayType != DayType.PADDING &&
                        it.startDate <= item.date &&
                        item.date <= it.endDate
            }

            val scheduleListContainer = arrayOfNulls<TextView>(MAX_VISIBLE_SCHEDULE_SIZE)
            filteredScheduleList.forEach {
                val isFirstShowSchedule = item.date == it.startDate || item.dayType == DayType.SUNDAY
                val paddingKey = "${adapterPosition / CALENDAR_COLUMN_SIZE}:${it.id}"
                val paddingLineIndex = lineIndex[paddingKey]

                if (paddingLineIndex != null) {
                    scheduleListContainer[paddingLineIndex] = mappingScheduleTextView(it, isFirstShowSchedule)
                } else {
                    for (i in 0 until MAX_VISIBLE_SCHEDULE_SIZE) {
                        if (scheduleListContainer[i] == null) {
                            scheduleListContainer[i] = mappingScheduleTextView(it, isFirstShowSchedule)
                            lineIndex[paddingKey] = i
                            break
                        }
                    }
                }
            }
            return scheduleListContainer
        }

        private fun mappingScheduleTextView(it: CalendarScheduleObject, isFirstShowSchedule: Boolean): TextView {
            val textView = makeDefaultScheduleTextView()
            textView.text = if (isFirstShowSchedule) it.text else ""
            textView.setTextColor(Color.WHITE)
            textView.setBackgroundColor(it.color)
            return textView
        }

        private fun makeDefaultScheduleTextView(): TextView {
            val textView = TextView(itemView.context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, context().dp2px(2.0f).toInt())
            textView.isSingleLine = true
            textView.layoutParams = layoutParams
            textView.height = calculateHeight / SCHEDULE_HEIGHT_DIVIDE_RATIO
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.setPadding(context().dp2px(2.0f).toInt(), 0, 0, 0)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            return textView
        }

        private fun notifyChangedSelectPosition(position: Int) {
            val selectedTemp = selectedPosition
            selectedPosition = position

            if (selectedTemp != -1) {
                currentList[selectedTemp].isSelected = false
                notifyItemChanged(selectedTemp)
            }

            currentList[position].isSelected = true
            notifyItemChanged(position)
        }

        private fun notifyClickEventType() {
            if (selectedPosition == adapterPosition) {
                onDateSecondClickListener?.onDayClick(currentList[adapterPosition].date, adapterPosition)
            } else {
                onDateClickListener?.onDayClick(currentList[adapterPosition].date, adapterPosition)
            }
        }
    }

    companion object {

        private val diffUtil = object : DiffUtil.ItemCallback<CalendarDate>() {

            override fun areItemsTheSame(oldItem: CalendarDate, newItem: CalendarDate) = oldItem.date == newItem.date

            override fun areContentsTheSame(oldItem: CalendarDate, newItem: CalendarDate) = oldItem == newItem
        }

        private const val CALENDAR_COLUMN_SIZE = 7

        private const val SCHEDULE_HEIGHT_DIVIDE_RATIO = 6

        private const val MAX_VISIBLE_SCHEDULE_SIZE = 3
    }
}
