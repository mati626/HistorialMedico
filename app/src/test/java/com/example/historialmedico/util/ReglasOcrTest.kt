package com.example.historialmedico.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ReglasOcrTest {

    @Test
    fun palabrasANumeros_dosisEnPalabras() {
        //el nombre conserva su tilde, solo cambia el numero
        assertEquals("Losartán 50 mg", ReglasOcr.palabrasANumeros("Losartán cincuenta mg"))
    }

    @Test
    fun palabrasANumeros_compuestoConY() {
        assertEquals("132", ReglasOcr.palabrasANumeros("ciento treinta y dos"))
    }

    @Test
    fun palabrasANumeros_miles() {
        assertEquals("2500 mg", ReglasOcr.palabrasANumeros("dos mil quinientos mg"))
    }

    @Test
    fun palabrasANumeros_sinTildeYMayusculas() {
        //el OCR puede perder la tilde o leer todo en mayusculas
        assertEquals("16", ReglasOcr.palabrasANumeros("dieciseis"))
        assertEquals("16", ReglasOcr.palabrasANumeros("DIECISÉIS"))
        assertEquals("22", ReglasOcr.palabrasANumeros("veintidós"))
    }

    @Test
    fun palabrasANumeros_parteDeOtraPalabra_noCambia() {
        //"dos" dentro de "Dosis" y "una" dentro de "Unasyn" no son numeros
        assertEquals("Dosis", ReglasOcr.palabrasANumeros("Dosis"))
        assertEquals("Unasyn", ReglasOcr.palabrasANumeros("Unasyn"))
        assertEquals("", ReglasOcr.palabrasANumeros(""))
    }

    @Test
    fun palabrasANumeros_dosNumerosSeguidos() {
        //asi se lee una presion escrita: sistolica y diastolica juntas
        assertEquals("120 80", ReglasOcr.palabrasANumeros("ciento veinte ochenta"))
    }

    @Test
    fun palabrasANumeros_ordenInvalido_separa() {
        //"tres" no cabe despues de "dos": son dos numeros, no 5
        assertEquals("2 3", ReglasOcr.palabrasANumeros("dos tres"))
    }

    @Test
    fun palabrasANumeros_yQueNoEsNumero_seMantiene() {
        //la "y" solo se une si despues viene otro numero
        assertEquals("5 y media", ReglasOcr.palabrasANumeros("cinco y media"))
    }

    @Test
    fun palabrasANumeros_saltoDeLinea_noUne() {
        //cada linea leida es un dato distinto: no debe quedar 72
        assertEquals("70\n2", ReglasOcr.palabrasANumeros("setenta\ndos"))
    }

    @Test
    fun leerMedicamento_cajaCompleta() {
        //el nombre sale de la linea de la dosis, aunque la marca este mas grande
        val lineas = listOf(
            LineaLeida("LOSARTAN", 80),
            LineaLeida("Losartán potásico 50 mg", 30),
            LineaLeida("30 comprimidos recubiertos", 25)
        )
        assertEquals(
            MedicamentoLeido("Losartán potásico", "50 mg", 30),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_dosisYCantidadEnPalabras() {
        val lineas = listOf(
            LineaLeida("Paracetamol quinientos miligramos", 40),
            LineaLeida("treinta comprimidos", 20)
        )
        assertEquals(
            MedicamentoLeido("Paracetamol", "500 mg", 30),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_nombreEnMayusculas() {
        //queda como oracion, pero "D3" no se toca
        assertEquals(
            MedicamentoLeido("Metformina", "850 mg", null),
            ReglasOcr.leerMedicamento(listOf(LineaLeida("METFORMINA 850 MG", 40)))
        )
        assertEquals(
            MedicamentoLeido("Vitamina D3", "1000 UI", null),
            ReglasOcr.leerMedicamento(listOf(LineaLeida("VITAMINA D3 1000 UI", 40)))
        )
    }

    @Test
    fun leerMedicamento_recetaConPrefijo() {
        //el nombre del medico es mas grande, pero el medicamento va pegado a su dosis
        val leido = ReglasOcr.leerMedicamento(
            listOf(
                LineaLeida("Dr. Juan Perez", 60),
                LineaLeida("Rp: Enalapril 10 mg", 30),
                LineaLeida("Tomar 1 comprimido cada 12 horas", 25)
            )
        )
        assertEquals("Enalapril", leido.nombre)
        assertEquals("10 mg", leido.dosis)
    }

    @Test
    fun leerMedicamento_dosisSola_nombreDeLaLineaMasGrande() {
        val lineas = listOf(
            LineaLeida("50 mg", 30),
            LineaLeida("COZAAR", 90),
            LineaLeida("Venta bajo receta", 20)
        )
        assertEquals(
            MedicamentoLeido("Cozaar", "50 mg", null),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_dosisConDecimal() {
        assertEquals(
            MedicamentoLeido("Bisoprolol", "2,5 mg", null),
            ReglasOcr.leerMedicamento(listOf(LineaLeida("Bisoprolol 2,5 mg", 30)))
        )
    }

    @Test
    fun leerMedicamento_variasCantidades_ganaLaMayor() {
        val lineas = listOf(
            LineaLeida("Tomar 1 comprimido al día", 20),
            LineaLeida("Caja con 28 comprimidos", 20)
        )
        assertEquals(28, ReglasOcr.leerMedicamento(lineas).cantidad)
    }

    @Test
    fun leerMedicamento_cantidadSobreElMaximo_null() {
        //1000 pasa el maximo de 999 del formulario
        val lineas = listOf(
            LineaLeida("Aspirina", 20),
            LineaLeida("1000 comprimidos", 20)
        )
        assertEquals(
            MedicamentoLeido("Aspirina", null, null),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_gotasNoEsDosis() {
        //la "g" de "gotas" no es gramos
        val lineas = listOf(
            LineaLeida("Ibuprofeno", 40),
            LineaLeida("Tomar 10 gotas", 20)
        )
        assertEquals(
            MedicamentoLeido("Ibuprofeno", null, null),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_nombreSobreElMaximo_null() {
        //60 letras: el formulario admite maximo 50
        val lineas = listOf(LineaLeida("A".repeat(60) + " 50 mg", 30))
        assertEquals(
            MedicamentoLeido(null, "50 mg", null),
            ReglasOcr.leerMedicamento(lineas)
        )
    }

    @Test
    fun leerMedicamento_sinTextoUtil_todoNull() {
        val nada = MedicamentoLeido(null, null, null)
        assertEquals(nada, ReglasOcr.leerMedicamento(emptyList()))
        assertEquals(
            nada,
            ReglasOcr.leerMedicamento(listOf(LineaLeida("###", 10), LineaLeida("12", 10)))
        )
    }
}