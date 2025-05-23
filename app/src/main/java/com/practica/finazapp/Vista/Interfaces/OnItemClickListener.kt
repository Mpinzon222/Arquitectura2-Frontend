package com.practica.finazapp.Vista.Interfaces

import com.practica.finazapp.Entidades.GastoDTO

interface OnItemClickListener {
    fun onItemClick(gasto: GastoDTO)
}