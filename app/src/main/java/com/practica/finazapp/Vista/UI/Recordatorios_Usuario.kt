package com.practica.finazapp.Vista.UI

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.practica.finazapp.Entidades.RecordatorioDTO
import com.practica.finazapp.R
import com.practica.finazapp.ViewModelsApiRest.ReminderViewModel
import com.practica.finazapp.ViewModelsApiRest.SharedViewModel
import com.practica.finazapp.Vista.Adapters.RecordatorioAdapter
import com.practica.finazapp.Vista.Interfaces.RecordatorioListener
import com.practica.finazapp.notificaciones.RecordatorioReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Recordatorios_Usuario : AppCompatActivity() , RecordatorioListener {

    private var usuarioId: Long = -1
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var recordatoriosViewModel: ReminderViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordatorioAdapter
    private lateinit var imgNoGastos1: LottieAnimationView
    private lateinit var tv_no_recordatorios: TextView
    private var recordatoriosAnteriores: List<RecordatorioDTO> = emptyList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordatorios_usuario)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        recordatoriosViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)
        recyclerView = findViewById(R.id.recyclerViewRecordatorios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        usuarioId = intent.getLongExtra("usuarioId", -1)

        imgNoGastos1 = findViewById(R.id.lottie_no_gastos)
        tv_no_recordatorios = findViewById(R.id.tv_no_recordatorios)

        fetchRecordatorios()

        recordatoriosViewModel.operacionCompletada.observe(this) { completada ->
            if (completada == true) {
                fetchRecordatorios()
            }
        }

        val btnNuevoRecordatorio = findViewById<ImageView>(R.id.NuevoRecordatorio)
        val btnEliminarRecordatorio = findViewById<ImageView>(R.id.eliminarRecordatorios)
        val btnActualizarListaporNombre = findViewById<ImageView>(R.id.BuscarPornombre)
        val btnActualizarLista = findViewById<ImageView>(R.id.ActualizarRecordatorios)
        val btndevolver = findViewById<ImageView>(R.id.devolverse)

        btnEliminarRecordatorio.setOnClickListener {
            Advertencia()
        }

        btnActualizarListaporNombre.setOnClickListener {
            BuscarPorNombre()
        }

        btnActualizarLista.setOnClickListener {
            fetchRecordatorios()
        }

        btndevolver.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        btnNuevoRecordatorio.setOnClickListener {
            Log.d("Recordatorio", "Botón Nuevo Recordatorio presionado")

            // Inflar el layout del diálogo
            val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_recordatorio, null)
            Log.d("Recordatorio", "Layout del diálogo inflado correctamente")

            // Obtener referencias de los elementos del diálogo
            val editTextNombre = dialogView.findViewById<TextInputEditText>(R.id.Busca_recordatorio)
            Log.d("Recordatorio", "Referencia a editTextNombre obtenida")
            val spinnerEstado = dialogView.findViewById<Spinner>(R.id.spinnerDescripcion1)
            Log.d("Recordatorio", "Referencia a spinnerEstado obtenida")
            val spinnerDias = dialogView.findViewById<Spinner>(R.id.spinnerDescripcion2)
            Log.d("Recordatorio", "Referencia a spinnerDias obtenida")
            val editTextFecha = dialogView.findViewById<EditText>(R.id.editTextFecha1)
            Log.d("Recordatorio", "Referencia a editTextFecha obtenida")
            val editTextCantidad =
                dialogView.findViewById<TextInputEditText>(R.id.editTextRecordatoriolimite)
            Log.d("Recordatorio", "Referencia a editTextCantidad obtenida")

            // Configurar el Spinner de estados
            val estados = listOf("Activo")
            val adapter = ArrayAdapter(this, R.layout.item_spinner_recordatorios, estados)
            adapter.setDropDownViewResource(R.layout.item_spinner_recordatorios)
            spinnerEstado.adapter = adapter
            Log.d("Recordatorio", "Spinner de estados configurado con: $estados")

            // Configurar el Spinner de días
            val diasOpciones = listOf("Cada 2 minutos", "Cada 3 minutos", "Cada 5 minutos")
            val adapterDias = ArrayAdapter(this, R.layout.item_spinner_dias, diasOpciones)
            adapterDias.setDropDownViewResource(R.layout.item_spinner_dias)
            spinnerDias.adapter = adapterDias
            Log.d("Recordatorio", "Spinner de días configurado con: $diasOpciones")

            // Configurar el selector de fecha
            editTextFecha.setOnClickListener {
                Log.d("Recordatorio", "Campo de fecha presionado, mostrando DatePicker")
                showDatePickerDialog(editTextFecha)
            }

            // Construir el diálogo
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(
                    "Guardar",
                    null
                ) // Se asignará más tarde para no cerrar el diálogo automáticamente
                .setNegativeButton("Cancelar") { dialog, _ ->
                    Log.d(
                        "Recordatorio",
                        "Botón Cancelar del diálogo presionado, se cierra el diálogo"
                    )
                    dialog.dismiss()
                }
                .create()

            // Mostrar el diálogo
            dialog.show()
            Log.d("Recordatorio", "Diálogo mostrado en pantalla")

            // Sobrescribir el comportamiento del botón "Guardar"
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                Log.d("Recordatorio", "Botón Guardar del diálogo presionado")

                // Recoger los valores ingresados
                val nombrerecordatorio = editTextNombre.text.toString().trim()
                val estado = spinnerEstado.selectedItem.toString().trim()
                val diasSeleccionado = spinnerDias.selectedItem.toString().trim()
                val fechaOriginal = editTextFecha.text.toString().trim()
                val limiteStr = editTextCantidad.text.toString().trim()

                Log.d(
                    "Recordatorio",
                    "Valores ingresados - Nombre: '$nombrerecordatorio', Estado: '$estado', " +
                            "Días: '$diasSeleccionado', Fecha: '$fechaOriginal', Límite: '$limiteStr'"
                )

                // Validar que no haya campos vacíos
                if (nombrerecordatorio.isBlank() || fechaOriginal.isBlank() || limiteStr.isBlank()) {
                    Log.e("Recordatorio", "Error: Se encontraron campos vacíos")
                    AlertDialog.Builder(this)
                        .setTitle("Campos vacíos")
                        .setMessage("Por favor, llene todos los campos antes de guardar.")
                        .setPositiveButton("OK") { alertDialog, _ -> alertDialog.dismiss() }
                        .create()
                        .show()
                    return@setOnClickListener
                }

                // Validar el formato de la fecha
                val parts = fechaOriginal.split("/")
                if (parts.size < 3) {
                    Log.e("Recordatorio", "Formato de fecha incorrecto: $fechaOriginal")
                    AlertDialog.Builder(this)
                        .setTitle("Formato de fecha incorrecto")
                        .setMessage("El formato de la fecha debe ser dd/MM/yyyy.")
                        .setPositiveButton("OK") { alertDialog, _ -> alertDialog.dismiss() }
                        .create()
                        .show()
                    return@setOnClickListener
                }
                val dia = parts[0].padStart(2, '0')
                val mes = parts[1].padStart(2, '0')
                val anio = parts[2]
                val fecha = "${anio}-${mes}-${dia}"
                Log.d("Recordatorio", "Fecha formateada: $fecha")

                // Convertir la opción de días a milisegundos
                val dias = when (diasSeleccionado) {
                    "Cada 2 minutos" -> 2 * 60 * 1000L  // 2 minutos en milisegundos
                    "Cada 3 minutos" -> 3 * 60 * 1000L  // 3 minutos en milisegundos
                    "Cada 5 minutos" -> 5 * 60 * 1000L  // 5 minutos en milisegundos
                    else -> {
                        Log.e("Recordatorio", "Opción de días no reconocida: $diasSeleccionado")
                        0L
                    }
                }

                // Convertir el límite a Double, manejando posibles errores
                val valorDouble = try {
                    limiteStr.toDouble().also {
                        Log.d("Recordatorio", "Límite convertido a Double: $it")
                    }
                } catch (e: NumberFormatException) {
                    Log.e("Recordatorio", "Error al convertir límite a Double: $limiteStr", e)
                    AlertDialog.Builder(this)
                        .setTitle("Límite inválido")
                        .setMessage("Ingrese un valor numérico válido para el límite.")
                        .setPositiveButton("OK") { alertDialog, _ -> alertDialog.dismiss() }
                        .create()
                        .show()
                    return@setOnClickListener
                }

                // Crear el objeto RecordatorioDTO
                val nuevoRecordatorio = RecordatorioDTO(
                    nombre = nombrerecordatorio,
                    estado = estado,
                    dias_recordatorio = dias,
                    valor = valorDouble,
                    fecha = fecha
                )
                Log.d("Recordatorio", "RecordatorioDTO creado: $nuevoRecordatorio")

                // Registrar el recordatorio mediante el ViewModel
                recordatoriosViewModel.registrarRecordatorio(usuarioId, nuevoRecordatorio)
                Log.d("Recordatorio", "Recordatorio enviado al ViewModel para su registro")

                // Cerrar el diálogo después de guardar
                dialog.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        // No hagas nada para bloquear el botón atrás
    }

    private fun Advertencia() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar todos los recordatorios? Esta acción no se puede deshacer.")

        builder.setPositiveButton("Sí") { _, _ ->
            eliminarTodosLosRecordatorios()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Cierra el diálogo sin hacer nada
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun eliminarTodosLosRecordatorios() {
        if (usuarioId == -1L) {
            Toast.makeText(this, "Error: usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

            recordatoriosViewModel.eliminarTodos(usuarioId)
            recordatoriosViewModel.operacionCompletada.observe(this) { completada ->
                if (completada == true) {
                    Log.d("Recordatorios", "Todos los recordatorios eliminados correctamente.")
                    fetchRecordatorios() // Actualizar la UI después de eliminar
                }
            }

    }


    private fun BuscarPorNombre() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_buscar_recordatorio, null)
        val editTextNombre = dialogView.findViewById<EditText>(R.id.Busca_recordatorio3)
        val botonBuscar = dialogView.findViewById<Button>(R.id.button2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()

        botonBuscar.setOnClickListener {
            val nombre = editTextNombre.text.toString().trim()

            if (nombre.isNotBlank()) {
                recordatoriosViewModel.buscarPorNombre(nombre)
                recordatoriosViewModel.recordatorioLiveData.observe(this) { recordatorios ->
                    if (!recordatorios.isNullOrEmpty()) {
                        adapter = RecordatorioAdapter(recordatorios)
                        adapter.setOnItemClickListener3(this)
                        recyclerView.adapter = adapter
                        recyclerView.visibility = View.VISIBLE
                        imgNoGastos1.visibility = View.GONE
                        tv_no_recordatorios.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.GONE
                        imgNoGastos1.visibility = View.VISIBLE
                        tv_no_recordatorios.visibility = View.VISIBLE
                    }
                }
            } else {
                Toast.makeText(this, "Ingrese un nombre válido", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun isAlarmaProgramada(recordatorio: RecordatorioDTO): Boolean {
        val intent = Intent(this, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            recordatorio.id_recordatorio!!.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

        private fun fetchRecordatorios() {
            usuarioId = intent.getLongExtra("usuarioId", -1)
            Log.d("Recordatorios", "Usuario ID recibido: $usuarioId")

            recordatoriosViewModel.listarRecordatorios(usuarioId)
            Log.d("Recordatorios", "Llamado a listarRecordatorios()")

            recordatoriosViewModel.recordatoriosLiveData.observe(this) { recordatorioslist ->
                Log.d("Recordatorios", "Lista de recordatorios recibida: ${recordatorioslist?.size ?: 0} elementos")

                // Cancelar alarmas obsoletas
                recordatoriosAnteriores.filter { it !in (recordatorioslist ?: emptyList()) }.forEach { cancelarAlarma(it) }

                if (!recordatorioslist.isNullOrEmpty()) {
                    adapter = RecordatorioAdapter(recordatorioslist)
                    adapter.setOnItemClickListener3(this)
                    recyclerView.adapter = adapter
                    recyclerView.visibility = View.VISIBLE
                    imgNoGastos1.visibility = View.GONE
                    tv_no_recordatorios.visibility = View.GONE

                    // Reprogramar alarmas necesarias
                    recordatorioslist.forEach { recordatorio ->
                        if (!isAlarmaProgramada(recordatorio)) {
                            reprogramarAlarma(recordatorio)
                        }
                    }

                    recordatoriosAnteriores = recordatorioslist
                } else {
                    recyclerView.visibility = View.GONE
                    imgNoGastos1.visibility = View.VISIBLE
                    tv_no_recordatorios.visibility = View.VISIBLE
                    recordatoriosAnteriores = emptyList()
                }
            }
        }

    @SuppressLint("ScheduleExactAlarm")
    private fun reprogramarAlarma(recordatorio: RecordatorioDTO) {
        // Verificar si el estado del recordatorio es "Activo"
        if (recordatorio.estado == "Activo") {
            val prefs = getSharedPreferences("RecordatoriosPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("alarma_cancelada_${recordatorio.id_recordatorio}").apply()
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, RecordatorioReceiver::class.java).apply {
                putExtra("id_recordatorio", recordatorio.id_recordatorio!!.toInt())
                putExtra("nombre", recordatorio.nombre)
                putExtra("valor", recordatorio.valor.toString())
                putExtra("intervalo", recordatorio.dias_recordatorio)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                recordatorio.id_recordatorio!!.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val siguienteAlarma = System.currentTimeMillis() + recordatorio.dias_recordatorio

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                siguienteAlarma,
                pendingIntent
            )

            Log.d("Recordatorios", "Alarma reprogramada para ${recordatorio.nombre} en ${Date(siguienteAlarma)}")
        } else {
            Log.d("Recordatorios", "La alarma no se reprogramará porque el estado del recordatorio no es 'Activo'.")
        }
    }

    private fun cancelarAlarma(recordatorio: RecordatorioDTO) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, RecordatorioReceiver::class.java).apply {
            putExtra("id_recordatorio", recordatorio.id_recordatorio!!.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            recordatorio.id_recordatorio!!.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        val prefs = getSharedPreferences("RecordatoriosPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("alarma_cancelada_${recordatorio.id_recordatorio}", true).apply()

        Log.d("Recordatorios", "Alarma cancelada para: ${recordatorio.nombre}")
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onItemClick3(recordatorios: RecordatorioDTO) {
        Log.d("onItemClick", "Recordatorio clickeado: ${recordatorios.nombre}")

        val dialogView = layoutInflater.inflate(R.layout.dialog_modificar_recordatorio, null)
        val spinnerEstado = dialogView.findViewById<Spinner>(R.id.spinnerDescripcion3)
        val btnEliminar = dialogView.findViewById<Button>(R.id.button) // Botón eliminar

        val estados = listOf("Pagado", "Vencido")
        val adapterEstado = ArrayAdapter(this, R.layout.item_spinner_recordatorios, estados)
        adapterEstado.setDropDownViewResource(R.layout.item_spinner_recordatorios)
        spinnerEstado.adapter = adapterEstado
        spinnerEstado.setSelection(estados.indexOf(recordatorios.estado))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val prefs = getSharedPreferences("RecordatoriosPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("alarma_cancelada_${recordatorios.id_recordatorio}", true).apply()

                val estadoNuevo = spinnerEstado.selectedItem.toString().trim()
                val recordatorioActualizado = recordatorios.copy(estado = estadoNuevo)

                recordatoriosViewModel.modificarRecordatorio(recordatorios.id_recordatorio!!, recordatorioActualizado)
                reprogramarAlarma(recordatorioActualizado)
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()

        btnEliminar.setOnClickListener {
            recordatoriosViewModel.eliminarRecordatorios(recordatorios.id_recordatorio!!)
            cancelarAlarma(recordatorios)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showDatePickerDialog(editTextFecha: EditText) {
        // Obtener la fecha actual
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear y mostrar el DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year1, monthOfYear, dayOfMonth1 ->
                val fechaSeleccionada = "$dayOfMonth1/${monthOfYear + 1}/$year1"
                editTextFecha.setText(fechaSeleccionada)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this) // Elimina el observador después de la primera actualización
                observer.onChanged(value)
            }
        })
    }


}