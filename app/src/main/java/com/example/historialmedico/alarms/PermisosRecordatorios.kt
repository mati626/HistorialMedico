package com.example.historialmedico.alarms

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermisosRecordatorios {
    fun notificacionesActivas(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    //exenta del ahorro de bateria: android no retiene sus alarmas y permite que sean exactas
    fun bateriaSinRestriccion(context: Context): Boolean{
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun abrirAjustesNotificaciones(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, paquete(context))
        }
        abrir(context, intent)
    }

    //muestra el dialogo del sistema para quitar la restriccion
    fun pedirBateriaSinRestriccion(context: Context) {
        abrir(context, Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, paquete(context)))
    }

    private fun paquete(context: Context): Uri = Uri.parse("package:${context.packageName}")

    //si el celular no tiene esa pantalla, abre los ajustes generales de la app
    private fun abrir(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, paquete(context)))
        }
    }
}
