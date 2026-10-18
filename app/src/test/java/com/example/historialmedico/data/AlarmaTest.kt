package com.example.historialmedico.data

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows

class AlarmaTest {

    @Test
    fun alarma_sinMedicamentosNiHoraMedica_lanzaError(){
        assertThrows(IllegalArgumentException::class.java){
            Alarma(hora=480)
        }
    }

    @Test
    fun alarma_conMedicamentoYHoraMedica_lanzaError(){
        assertThrows(IllegalArgumentException::class.java){
            Alarma(medicamentoId = 1, horaMedicaId = 1, hora = 480)
        }
    }

    @Test
    fun alarma_horaFueraDelDia_lanzaError(){
        assertThrows(IllegalArgumentException::class.java){
            Alarma(medicamentoId = 1,hora=1440)
        }
    }

    @Test
    fun alarma_medicamentoConFecha_lanzaError(){
        assertThrows(IllegalArgumentException::class.java){
            Alarma(medicamentoId = 1, hora = 480, fechaHoraMillis = 0L)
        }
    }

    @Test
    fun alarma_medicamentoValido_seCrea(){
        val alarma = Alarma(medicamentoId = 1, hora =480)
        assertEquals(480, alarma.hora)
    }
}