package com.example.historialmedico.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas{
    private val ZONA_UTC: TimeZone= TimeZone.getTimeZone("UTC")

    //el selector de fecha elegido como medianoche en UTC
    fun utcMillisAIso(utcMillis: Long): String=
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone=ZONA_UTC }
            .format(Date(utcMillis))

    //"2026-10-18"->"18/10/2026"
    fun isoAVisible(iso: String): String{
        val partes=iso.split("-")
        return if (partes.size==3)"${partes[2]}/${partes[1]}/${partes[0]}" else iso
    }

    //union de dia elegido con hora y minutos locales
    fun combinar(utcMillisFecha: Long,hora: Int,minuto: Int): Long{
        val dia= Calendar.getInstance(ZONA_UTC).apply { timeInMillis=utcMillisFecha }
        return Calendar.getInstance().apply {
            clear()
            set(
                dia.get(Calendar.YEAR),
                dia.get(Calendar.MONTH),
                dia.get(Calendar.DAY_OF_MONTH),
                hora,
                minuto
            )
        }.timeInMillis
    }

    //milisegundos ->"18/10/2026 09:30"
    fun fechaHoraVisible(millis: Long): String= SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(millis))

    //480->"08:00"
    fun minutosAHora(minutos: Int): String=
        String.format(Locale.US,"%02d:%02d",minutos/60,minutos%60)
}