package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import kotlin.Double.Companion.NEGATIVE_INFINITY

class SaveReminderViewModel(
    val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val startGeofence = MutableLiveData<ReminderDataItem?>()
    private var latitude: Double = NEGATIVE_INFINITY
    private var longitude: Double = NEGATIVE_INFINITY

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = ""
        reminderDescription.value = ""
        reminderSelectedLocationStr.value = ""
        startGeofence.value = null
        latitude = NEGATIVE_INFINITY
        longitude = NEGATIVE_INFINITY
    }

    /**
     * Save the reminder to the data source
     */
    fun onGeofenceCompleted(reminderData: ReminderDataItem) {
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            onClear()
            navigationCommand.value = NavigationCommand.Back
        }
    }

    fun onGeofenceFailed(){
        showLoading.value = false
        showToast.value = app.getString(R.string.geofence_not_available)
    }

    fun onSaveReminder() {
        when {
            reminderTitle.value.isNullOrEmpty() -> {
                showSnackBarInt.value = R.string.err_enter_title
            }
            reminderSelectedLocationStr.value.isNullOrEmpty() -> {
                showSnackBarInt.value = R.string.err_select_location
            }
            else -> {
                showLoading.value = true
                val reminder = ReminderDataItem(
                    reminderTitle.value,
                    reminderDescription.value,
                    reminderSelectedLocationStr.value,
                    latitude,
                    longitude,
                )
                startGeofence.value = reminder
            }
        }
    }

    fun onPermissionDenied() {
        showSnackBar.value = app.getString(R.string.location_required_error)
    }

    fun savePoi(poi: PointOfInterest) {
        reminderSelectedLocationStr.value = poi.name
        latitude = poi.latLng.latitude
        longitude = poi.latLng.longitude
    }

    fun saveDroppedPin(location: String, lat: Double, long: Double) {
        reminderSelectedLocationStr.value = location
        latitude = lat
        longitude = long
    }
}