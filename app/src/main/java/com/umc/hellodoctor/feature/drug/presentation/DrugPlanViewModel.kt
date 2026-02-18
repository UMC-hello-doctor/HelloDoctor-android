package com.umc.hellodoctor.feature.drug.presentation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanDao
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanEntity
import com.umc.hellodoctor.feature.drug.data.service.DrugAlarmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrugPlanViewModel @Inject constructor(
    private val drugPlanDao: DrugPlanDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val alarmManager = DrugAlarmManager(context)

    private val _plans = MutableLiveData<List<DrugPlanEntity>>(emptyList())
    val plans: LiveData<List<DrugPlanEntity>> get() = _plans

    private val _selectedPlan = MutableLiveData<DrugPlanEntity?>(null)
    val selectedPlan: LiveData<DrugPlanEntity?> get() = _selectedPlan

    fun loadAllPlans() {
        viewModelScope.launch {
            _plans.value = drugPlanDao.getAllPlans()
        }
    }

    fun loadPlan(planId: String) {
        viewModelScope.launch {
            _selectedPlan.value = drugPlanDao.getPlanById(planId)
        }
    }

    fun savePlan(plan: DrugPlanEntity) {
        viewModelScope.launch {
            drugPlanDao.insertPlan(plan)
            alarmManager.scheduleAlarms(plan.id, plan.alarms)
            _selectedPlan.value = plan
            _plans.value = drugPlanDao.getAllPlans()
        }
    }

    fun updatePlan(plan: DrugPlanEntity) {
        viewModelScope.launch {
            drugPlanDao.updatePlan(plan)
            alarmManager.cancelAlarms(plan.id, plan.alarms)
            alarmManager.scheduleAlarms(plan.id, plan.alarms)
            _selectedPlan.value = plan
            _plans.value = drugPlanDao.getAllPlans()
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            val plan = drugPlanDao.getPlanById(planId)
            if (plan != null) {
                alarmManager.cancelAlarms(plan.id, plan.alarms)
            }
            drugPlanDao.deletePlanById(planId)
            if (_selectedPlan.value?.id == planId) {
                _selectedPlan.value = null
            }
            _plans.value = drugPlanDao.getAllPlans()
        }
    }
}

