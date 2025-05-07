package com.practica.finazapp.Entidades

data class UsuarioDTO (

    var id_usuario: Long? = null,
    var username: String,
    var contrasena: String,
    var nombre: String,
    var email: String,
    var apellido: String,
    val roles: Set<String>

)