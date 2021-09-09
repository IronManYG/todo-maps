package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val TAG = "GeofenceTransitionsJob"

        private const val JOB_ID = 573

        //        : call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        // Get the transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that we triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // call @sendNotification
            sendNotification(triggeringGeofences)
            Log.i(TAG, "geofence present")
        }
    }

    //: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        var requestId = ""

        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                triggeringGeofences.forEach { geofence ->
                    requestId = geofence.requestId
                    //get the reminder with the request id
                    val result = remindersLocalRepository.getReminder(requestId)
                    if (result is Result.Success<ReminderDTO>) {
                        val reminderDTO = result.data
                        //send a notification to the user with the reminder details
                        sendNotification(
                            this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                                reminderDTO.title,
                                reminderDTO.description,
                                reminderDTO.location,
                                reminderDTO.latitude,
                                reminderDTO.longitude,
                                reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}