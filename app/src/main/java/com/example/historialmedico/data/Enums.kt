package com.example.historialmedico.data

enum class EstadoExamen(val etiqueta: String){
    SOLICITADO("Solicitado"),
    AGENDADO("Agendado"),
    REALIZADO("Realizado")
}

enum class Sexo(val etiqueta: String){
    FEMENINO("Femenino"),
    MASCULINO("Masculino"),
    OTRO("Otro")
}

enum class EstadoCivil(val etiqueta: String){
    SOLTERO("Soltero/a"),
    CASADO("Casado/a"),
    CONVIVIENTE_CIVIL("Conviviente civil"),
    SEPARADO("Separado/a"),
    DIVORCIADO("Divorciado/a"),
    VIUDO("Viudo/a")
}

enum class Prevision(val etiqueta: String){
    FONASA("Fonasa"),
    ISAPRE("Isapre"),
    CAPREDENA_DIPRECA("Capredena/Dipreca"),
    NINGUNA("Ninguna"),
    OTRA("Otra")
}

enum class GrupoSanguineo(val etiqueta: String){
    A_POSITIVO("A+"),
    A_NEGATIVO("A-"),
    B_POSITIVO("B+"),
    B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"),
    AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"),
    O_NEGATIVO("O-"),
}