package com.example.historialmedico.data

import com.example.historialmedico.util.Fechas

object ReglasExamen {
    fun estado(realizado: Boolean, fecha: String?): EstadoExamen = when {
        realizado -> EstadoExamen.REALIZADO
        fecha != null -> EstadoExamen.AGENDADO
        else -> EstadoExamen.SOLICITADO
    }

    fun validarFecha(realizado: Boolean, fecha: String?, hoy: String): String? = when {
        fecha == null -> if (realizado) "Indique la fecha en que se realizó el examen" else null
        realizado && fecha > hoy -> "Un examen realizado no puede tener fecha futura"
        !realizado && fecha < hoy -> "Un examen próximo no puede tener fecha pasada"
        else -> null
    }

    fun consultasPosibles(horas: List<HoraMedica>, ahora: Long):
            List<HoraMedica> = horas.filter { it.fechaHora <= ahora }

    fun validarConsulta(fecha: String?, fechaConsulta: String?): String? =
        if (fecha != null && fechaConsulta != null && fecha < fechaConsulta)
            "El examen no puede ser anterior a la consulta en que se pidió"
        else null

    fun textoConsulta(consulta: HoraMedica):
            String = "${consulta.especialidad}, ${Fechas.fechaHoraVisible(consulta.fechaHora)}"

    //al editar una consulta, los examenes pedidos en ella tienen que seguir siendo validos
    fun validarMoverConsulta(fechaHora: Long, examenes: List<Examen>, ahora: Long): String? = when {
        examenes.isEmpty() -> null
        //solo se piden examenes en consultas que ya pasaron, igual que en consultasPosibles
        fechaHora > ahora -> "Esta consulta tiene exámenes pedidos, no puede quedar en una fecha futura"
        examenes.any { validarConsulta(it.fecha, Fechas.millisAIso(fechaHora)) != null } ->
            "Hay un examen pedido en esta consulta con fecha anterior a la nueva fecha"
        else -> null
    }
}