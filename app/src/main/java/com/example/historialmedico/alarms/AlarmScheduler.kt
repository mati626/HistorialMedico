package com.example.historialmedico.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.historialmedico.data.Alarma
import java.util.Calendar

object AlarmScheduler {

    fun programar(context: Context, alarma: Alarma) {
        if (!alarma.activo) return
        val momento = proximoDisparo(alarma) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            momento,
            pendingIntent(context, alarma.id)
        )
    }

    fun cancelar(context: Context, alarmaId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, alarmaId))
    }

    private fun proximoDisparo(alarma: Alarma): Long? {
        val ahora = System.currentTimeMillis()
        val hora = alarma.hora
        if (hora != null) {
            val calendario = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hora / 60)
                set(Calendar.MINUTE, hora % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= ahora) add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendario.timeInMillis
        }
        val puntual = alarma.fechaHoraMillis
        return if (puntual != null && puntual > ahora) puntual else null
    }

    private fun pendingIntent(context: Context, alarmaId: Long): PendingIntent {
        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra(RecordatorioReceiver.EXTRA_ALARMA_ID, alarmaId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmaId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}