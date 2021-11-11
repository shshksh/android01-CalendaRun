package com.drunkenboys.calendarun.ui.dayschedule

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.drunkenboys.calendarun.R
import com.drunkenboys.calendarun.databinding.DialogDayScheduleBinding
import com.drunkenboys.calendarun.ui.saveschedule.model.BehaviorType
import com.drunkenboys.calendarun.ui.searchschedule.SearchScheduleAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DayScheduleDialog : DialogFragment() {

    private var _binding: DialogDayScheduleBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException()

    private val dayScheduleViewModel by viewModels<DayScheduleViewModel>()

    private val dayScheduleAdapter = SearchScheduleAdapter()

    private val navController by lazy { findNavController() }
    private val args by navArgs<DayScheduleDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        _binding = DialogDayScheduleBinding.inflate(layoutInflater)
        dayScheduleViewModel.fetchScheduleList(LocalDate.parse(args.localDate))

        initRvDaySchedule()
        initIvAddSchedule()
        observeListItem()
        observeScheduleClickEvent()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun initRvDaySchedule() {
        binding.rvDaySchedule.adapter = dayScheduleAdapter
    }

    private fun initIvAddSchedule() {
        binding.ivDayScheduleAddSchedule.setOnClickListener {
            val action = DayScheduleDialogDirections.actionDayScheduleDialogToSaveScheduleFragment(BehaviorType.INSERT)
            navController.navigate(action)
        }
    }

    private fun observeListItem() {
        dayScheduleViewModel.listItem.observe(this) { listItem ->
            dayScheduleAdapter.submitList(listItem)
        }
    }

    private fun observeScheduleClickEvent() {
        dayScheduleViewModel.scheduleClickEvent.observe(this) {
            val action = DayScheduleDialogDirections.actionDayScheduleDialogToSaveScheduleFragment(BehaviorType.UPDATE)
            navController.navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()

        val displayMetrics = resources.displayMetrics
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_white_radius_24dp)
        dialog?.window?.setLayout((displayMetrics.widthPixels * 0.9).toInt(), (displayMetrics.heightPixels * 0.7).toInt())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}