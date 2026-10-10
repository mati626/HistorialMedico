package com.example.historialmedico.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.historialmedico.data.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarmas = AppDataBase.getInstance(context).alarmaDao().getActivas()
                alarmas.forEach { alarma -> AlarmScheduler.programar(context, alarma) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}