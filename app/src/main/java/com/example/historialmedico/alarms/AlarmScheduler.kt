package com.example.historialmedico.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler{
    fun programarRecordatorioMedicamento(context: Context,medicamentoId: Long,nombre: String, hora: String){
        val partes=hora.split(":")
        val horaDelDia=partes[0].toInt()
        val minuto=partes[1].toInt()

        val calendar= Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,horaDelDia)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND,0)
            if(before(Calendar.getInstance())){
                add(Calendar.DAY_OF_YEAR,1)
            }
        }

        val intent= Intent(context, RecordatorioReceiver::class.java).apply{
            putExtra(RecordatorioReceiver.EXTRA_TITULO,"Hora de tomar tu medicamento")
            putExtra(RecordatorioReceiver.EXTRA_MENSAJE,nombre)
            putExtra(RecordatorioReceiver.EXTRA_TIPO, RecordatorioReceiver.TIPO_MEDICAMENTO)
            putExtra(RecordatorioReceiver.EXTRA_MEDICAMENTO_ID, medicamentoId)
            putExtra(RecordatorioReceiver.EXTRA_MEDICAMENTO_NOMBRE, nombre)
            putExtra(RecordatorioReceiver.EXTRA_MEDICAMENTO_HORA, hora)
        }

        val pendingIntent= PendingIntent.getBroadcast(
            context,
            medicamentoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as
                AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelarRecordatorioMedicamento(context: Context, medicamentoId: Long){
        val intent= Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent= PendingIntent.getBroadcast(
            context,
            medicamentoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun programarRecordatorioHoraMedica(context: Context,horaMedicaId: Long, especialidad: String, lugar: String, triggerAtMillis: Long){
        val intent= Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra(RecordatorioReceiver.EXTRA_TITULO,"Cita medica proxima")
            putExtra(RecordatorioReceiver.EXTRA_MENSAJE,"$especialidad en $lugar")
        }

        val pendingIntent= PendingIntent.getBroadcast(
            context,
            (horaMedicaId+100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancelarRecordatorioHoraMedica(context: Context,horaMedicaId: Long){
        val intent= Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent= PendingIntent.getBroadcast(
            context,
            (horaMedicaId+100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}