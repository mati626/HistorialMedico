package com.example.historialmedico.alarms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.historialmedico.MainActivity
import com.example.historialmedico.data.Alarma
import com.example.historialmedico.data.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmaId = intent.getLongExtra(EXTRA_ALARMA_ID, -1L)
        if (alarmaId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDataBase.getInstance(context)
                val alarma = database.alarmaDao().getById(alarmaId)
                if (alarma == null || !alarma.activo) return@launch

                val texto = textoNotificacion(database, alarma) ?: return@launch
                mostrarNotificacion(context, alarmaId, texto.first, texto.second)

                if (alarma.hora != null) {
                    AlarmScheduler.programar(context, alarma)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun textoNotificacion(database: AppDataBase, alarma: Alarma): Pair<String, String>? {
        val medicamentoId = alarma.medicamentoId
        if (medicamentoId != null) {
            val medicamento = database.medicamentoDao().getById(medicamentoId) ?: return null
            val paciente = database.pacienteDao().getById(medicamento.pacienteId) ?: return null
            return "Medicamento de ${paciente.nombre}" to "${medicamento.nombre} - ${medicamento.dosis}"
        }
        val horaMedicaId = alarma.horaMedicaId ?: return null
        val horaMedica = database.horaMedicaDao().getById(horaMedicaId) ?: return null
        val paciente = database.pacienteDao().getById(horaMedica.pacienteId) ?: return null
        return "Hora medica de ${paciente.nombre}" to "${horaMedica.especialidad} en ${horaMedica.lugar}"
    }

    private fun mostrarNotificacion(context: Context, alarmaId: Long, titulo: String, mensaje: String) {
        val permisoConcedido = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!permisoConcedido) return

        crearCanalNotificacion(context)

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(alarmaId.toInt(), notificacion)
    }

    private fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de medicamentos y horas medicas"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    companion object {
        const val EXTRA_ALARMA_ID = "extra_alarma_id"
        const val CANAL_ID = "recordatorios_canal"
    }
}