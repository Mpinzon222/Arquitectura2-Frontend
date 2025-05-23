package com.practica.finazapp.Vista.Interfaces

import com.practica.finazapp.Entidades.IngresoDTO

//Modificar ingresos mensuales
interface IngresosListener {

    fun onIngresosCargados(totalIngresos: Double, ingresosMensuales: List<IngresoDTO>, ingresosCasuales: List<IngresoDTO>)
}
