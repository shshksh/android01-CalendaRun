package com.drunkenboys.calendarun.ui.managecalendar

import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.setupWithNavController
import com.drunkenboys.calendarun.R
import com.drunkenboys.calendarun.databinding.FragmentManageCalendarBinding
import com.drunkenboys.calendarun.ui.base.BaseFragment
import com.drunkenboys.calendarun.util.extensions.launchAndRepeatWithViewLifecycle
import com.drunkenboys.calendarun.util.extensions.throttleFirst
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageCalendarFragment : BaseFragment<FragmentManageCalendarBinding>(R.layout.fragment_manage_calendar) {

    private val manageCalendarAdapter = ManageCalendarAdapter()

    private val manageCalendarViewModel by navGraphViewModels<ManageCalendarViewModel>(R.id.manageCalendarFragment) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = manageCalendarViewModel

        setupToolbar()
        setupAdapter()
        setupFabClickListener()
        setupToolbarMenuItemOnClickListener()

        launchAndRepeatWithViewLifecycle {
            launch { collectCalendarItemList() }
            launch { collectCalendarClickEvent() }
            launch { collectOpenDeleteDialog() }
            launch { collectCheckedCalendarNum() }
        }
    }

    private fun setupToolbar() {
        binding.toolbarManageCalendar.setupWithNavController(navController)
    }

    private fun setupAdapter() {
        binding.rvManageCalendar.adapter = manageCalendarAdapter
    }

    private fun setupFabClickListener() {
        binding.fabManagerCalenderAddCalendar.setOnClickListener {
            val action = ManageCalendarFragmentDirections.toSaveCalendar()
            navController.navigate(action)
        }
    }

    private fun setupToolbarMenuItemOnClickListener() {
        binding.toolbarManageCalendar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_delete_schedule) {
                manageCalendarViewModel.emitOpenDeleteDialogEvent(item.itemId)
                true
            } else {
                false
            }
        }
    }

    private suspend fun collectCalendarItemList() {
        manageCalendarViewModel.calendarItemList.collect { calendarItemList ->
            manageCalendarAdapter.submitList(calendarItemList)
        }
    }

    private suspend fun collectCalendarClickEvent() {
        manageCalendarViewModel.calendarClickEvent.collect { calendarId ->
            val action = ManageCalendarFragmentDirections.toEditCalendar(calendarId)
            navController.navigate(action)
        }
    }

    private suspend fun collectOpenDeleteDialog() {
        manageCalendarViewModel.openDeleteDialogEvent
            .throttleFirst(DEFAULT_TOUCH_THROTTLE_PERIOD)
            .collect {
                navController.navigate(ManageCalendarFragmentDirections.toDeleteCalendarDialog())
            }
    }

    private suspend fun collectCheckedCalendarNum() {
        manageCalendarAdapter.checkedCalendarNum.collect { nums ->
            binding.toolbarManageCalendar.menu.findItem(R.id.menu_delete_schedule).isVisible = nums > 0
        }
    }

    companion object {

        // TODO: 2021/12/13 리소스 분리 고려
        private const val DEFAULT_TOUCH_THROTTLE_PERIOD = 500L
    }
}
