package com.practica.finazapp.Entidades

data class RecordatorioDTO(

    val id_recordatorio: Long? = null,
    val nombre: String,
    val estado: String,
    val fecha: String,
    val dias_recordatorio: Long,
    val valor: Double
)
