package com.example.historialmedico.data

import androidx.room.Embedded
import androidx.room.Relation

//un paciente con sus medicamentos, Room trae los dos juntos para la pantalla de emergencia
data class PacienteEmergencia(
    @Embedded val paciente: Paciente,
    @Relation(parentColumn = "id", entityColumn = "pacienteId")
    val medicamentos: List<Medicamento>
)