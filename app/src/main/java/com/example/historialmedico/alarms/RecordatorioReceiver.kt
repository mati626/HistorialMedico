package com.example.historialmedico.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.historialmedico.MainActivity

class RecordatorioReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val titulo=intent.getStringExtra(EXTRA_TITULO)?:"Historial Medico"
        val mensaje=intent.getStringExtra(EXTRA_MENSAJE)?:""

        crearCanalNotificacion(context)

        val contentIntent= Intent(context, MainActivity:: class.java)
        val contentPendingIntent= PendingIntent.getActivity(
            context,0,contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification= NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        val permisoConcedido=Build.VERSION.SDK_INT<Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS)== PackageManager.PERMISSION_GRANTED

        if(permisoConcedido){
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(),notification)
        }
        if(intent.getStringExtra(EXTRA_TIPO)==TIPO_MEDICAMENTO){
            val medicamentoId=intent.getLongExtra(EXTRA_MEDICAMENTO_ID,-1L)
            val nombre= intent.getStringExtra(EXTRA_MEDICAMENTO_NOMBRE)
            val hora=intent.getStringExtra(EXTRA_MEDICAMENTO_HORA)
            if(medicamentoId!=-1L&&nombre!=null && hora !=null){
                AlarmScheduler.programarRecordatorioMedicamento(context,medicamentoId,nombre,hora)
            }
        }
    }

    private fun crearCanalNotificacion(context: Context){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val canal= NotificationChannel(
                CANAL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description="Notificaciones de medicamentos y horas medicas"
            }
            val manager=context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    companion object{
        const val EXTRA_TITULO="extra_titulo"
        const val EXTRA_MENSAJE="extra_mensaje"
        const val CANAL_ID="recordatorios_canal"
        const val EXTRA_TIPO="extra_tipo"
        const val EXTRA_MEDICAMENTO_ID="extra_medicamemto_id"
        const val EXTRA_MEDICAMENTO_NOMBRE="extra_medicamento_nombre"
        const val EXTRA_MEDICAMENTO_HORA="extra_medicamento_hora"
        const val TIPO_MEDICAMENTO="medicamento"

    }
}