package com.practica.finazapp.Vista.UI

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.practica.finazapp.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate")

        // Crear el canal de notificaciones
        crearCanalNotificacion(this)

        // Verificar si hay un token válido
        val token = getToken()
        if (token != null) {
            // Redirigir a la Activity principal (Dashboard)
            startActivity(Intent(this, Dashboard::class.java))
        } else {
            Log.d("MainActivity", "Token no encontrado")
            // Redirigir a la pantalla de inicio de sesión (Login)
            startActivity(Intent(this, Login::class.java))
        }

        // Cerrar la MainActivity
        finish()
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("MiApp", Context.MODE_PRIVATE)
        return sharedPreferences.getString("TOKEN", null)
    }


    private fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "CanalGastosAltos"
            val descripcion = "Notificaciones para gastos altos"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("GASTOS_ALTOS", nombre, importancia)
            canal.description = descripcion

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }
}