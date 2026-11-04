package com.example.historialmedico.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.historialmedico.data.Alarma
import com.example.historialmedico.data.AppDataBase
import java.util.Calendar

object AlarmScheduler {

    //FLAG_ONE_SHOT: al dispararse deja de existir, asi estaProgramada no confunde
    // una alarma que ya sono con una que sigue pendiente
    private const val FLAGS_ALARMA = PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    private const val UNA_HORA = 3_600_000L
    private const val UN_DIA = 24 * UNA_HORA

    fun programar(context: Context, alarma: Alarma){
        if(!alarma.activo) return
        val momento = proximoDisparo(alarma) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operacion = pendingIntent(context, alarma.id)
        //desde android 12 las alarmas exactas solo se permiten si la app esta exenta del ahorro de bateria
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, operacion)
        }else{
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, operacion)
        }
    }

    fun cancelar(context: Context, alarmaId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, alarmaId))
    }

    suspend fun reprogramarTodas(context: Context){
        val alarmas= AppDataBase.getInstance(context).alarmaDao().getActivas()
        alarmas.forEach { alarma -> programar(context, alarma) }
    }

    //al abrir la app no se tocan las ya programadas:
    // el recordatorio de hoy aun espera su turno, se cambiaria para mañana y se perderia
    suspend fun reprogramarFaltantes(context: Context){
        val alarmas = AppDataBase.getInstance(context).alarmaDao().getActivas()
        alarmas.filterNot { estaProgramada(context,it.id) }
            .forEach { alarma -> programar(context,alarma) }
    }

    //al conceder la exencion las alarmas ya programadas pasan a exactas, salvo la que aun
    //espera dentro de su margen: reprogramarla la cambiaria para manana y se perderia
    suspend fun volverExactas(context: Context) {
        val ahora = System.currentTimeMillis()
        val alarmas = AppDataBase.getInstance(context).alarmaDao().getActivas()
        alarmas.filterNot { enMargen(it, ahora) }
            .forEach { alarma -> programar(context, alarma) }
    }

    //true si su hora paso hace menos de 1 hora, el margen maximo de android; el + UN_DIA
    //cubre una alarma de las 23:30 revisada a las 00:10
    private fun enMargen(alarma: Alarma, ahora: Long): Boolean {
        val hora = alarma.hora ?: return false
        return (ahora - horaDeHoy(hora).timeInMillis + UN_DIA) % UN_DIA < UNA_HORA
    }

    //FLAG_NO_CREATE no crea nada: devuelve null si no existe
    private fun estaProgramada(context: Context, alarmaId: Long): Boolean=
        PendingIntent.getBroadcast(
            context,
            alarmaId.toInt(),
            Intent(context,
                RecordatorioReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or FLAGS_ALARMA
        )!=null

    private fun proximoDisparo(alarma: Alarma): Long? {
        val ahora = System.currentTimeMillis()
        val hora = alarma.hora
        if (hora != null) {
            val calendario = horaDeHoy(hora)
            if (calendario.timeInMillis <= ahora) calendario.add(Calendar.DAY_OF_YEAR, 1)
            return calendario.timeInMillis
        }
        val puntual = alarma.fechaHoraMillis
        return if (puntual != null && puntual > ahora) puntual else null
    }

    private fun horaDeHoy(hora: Int): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora / 60)
        set(Calendar.MINUTE, hora % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun pendingIntent(context: Context, alarmaId: Long): PendingIntent {
        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra(RecordatorioReceiver.EXTRA_ALARMA_ID, alarmaId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmaId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or FLAGS_ALARMA
        )
    }
}