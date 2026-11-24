package com.example.historialmedico.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.ParseException

object Fechas{
    private val ZONA_UTC: TimeZone= TimeZone.getTimeZone("UTC")

    //el selector de fecha elegido como medianoche en UTC
    fun utcMillisAIso(utcMillis: Long): String=
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone=ZONA_UTC }
            .format(Date(utcMillis))

    //"2026-10-18" -> medianoche UTC de ese dia, el formato que usa el selector de fecha
    fun isoAUtcMillis(iso: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .apply { timeZone = ZONA_UTC }
            .parse(iso)?.time
    } catch (e: ParseException) {
        null
    }

    //"2026-10-18"->"18/10/2026"
    fun isoAVisible(iso: String): String{
        val partes=iso.split("-")
        return if (partes.size==3)"${partes[2]}/${partes[1]}/${partes[0]}" else iso
    }

    //"2026-11-21"-> dia de la semana, dia y mes en español, con mayuscula al inicio
    //se formatea en UTC igual que se leyo, con la zona del celular saldria el dia anterior
    fun diaVisible(iso: String): String {
        val millis = isoAUtcMillis(iso) ?: return iso
        val espanol = Locale.forLanguageTag("es-CL")
        return SimpleDateFormat("EEEE d 'de' MMMM", espanol)
            .apply { timeZone = ZONA_UTC }
            .format(Date(millis))
            .replaceFirstChar { it.titlecase(espanol) }
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

    //fecha de hoy
    fun hoyIso(): String=
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    //milisegundos -> "2026-10-18" (dia de la zona del celular)
    fun millisAIso(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    //minutos desde medianoche en la zona del celular, 08:30 da 510
    fun minutosDelDia(millis: Long): Int {
        val calendario = Calendar.getInstance().apply { timeInMillis = millis }
        return calendario.get(Calendar.HOUR_OF_DAY) * 60 + calendario.get(Calendar.MINUTE)
    }
}