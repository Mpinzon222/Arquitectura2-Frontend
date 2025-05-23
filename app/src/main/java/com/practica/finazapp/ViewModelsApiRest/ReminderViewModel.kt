package com.practica.finazapp.ViewModelsApiRest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practica.finazapp.Entidades.RecordatorioDTO
import com.practica.finazapp.RepositoriosApiRest.ReminderRepository

class ReminderViewModel (application: Application) : AndroidViewModel(application) {

    private val recordatorioRepository: ReminderRepository by lazy {
        ReminderRepository(getApplication<Application>().applicationContext)
    }

    private val _recordatorioLiveData = MutableLiveData<List<RecordatorioDTO>?>()
    val recordatorioLiveData: LiveData<List<RecordatorioDTO>?> = _recordatorioLiveData

    private val _recordatoriosLiveData = MutableLiveData<List<RecordatorioDTO>?>()
    val recordatoriosLiveData: LiveData<List<RecordatorioDTO>?> = _recordatoriosLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    private val _operacionCompletada = MutableLiveData<Boolean>()
    val operacionCompletada: LiveData<Boolean> = _operacionCompletada


    // ✅ Registrar un nuevo recordatorio
    fun registrarRecordatorio(idUsuario: Long, recordatorioDTO: RecordatorioDTO) {
        recordatorioRepository.registrarRecordatorio(
            idUsuario,
            recordatorioDTO
        ) { recordatorio, error ->
            if (recordatorio != null) {
                _operacionCompletada.postValue(true)
            }
            _errorLiveData.postValue(error)
        }
    }

    // ✅ Obtener todos los recordatorios de un usuario
    fun listarRecordatorios(idUsuario: Long) {
        recordatorioRepository.listarRecordatorios(idUsuario) { recordatorios, error ->
            _recordatoriosLiveData.postValue(recordatorios)
            _errorLiveData.postValue(error)
        }
    }

    // ✅ Modificar un recordatorio
    fun modificarRecordatorio(idRecordatorio: Long, recordatorioDTO: RecordatorioDTO) {
        recordatorioRepository.modificarRecordatorio(
            idRecordatorio,
            recordatorioDTO
        ) { recordatorio, error ->
            if (recordatorio != null) {
                _operacionCompletada.postValue(true)
            } else {
                _errorLiveData.postValue(error)
            }
        }
    }

    // ✅ Eliminar un recordatorio
    fun eliminarRecordatorios(idRecordatorio: Long) {
        recordatorioRepository.eliminarRecordatorio(idRecordatorio) { success, error ->
            if (success) {
                _operacionCompletada.postValue(true)
            } else {
                _errorLiveData.postValue(error)
            }
        }
    }

    // ✅ Eliminar todos los recordatorios
    fun eliminarTodos(idUsuario: Long) {
        recordatorioRepository.eliminarRecordatorios(idUsuario) { success, error ->
            if (success != null) {
                _operacionCompletada.postValue(true)
            } else {
                _errorLiveData.postValue(error)
            }

        }
    }
        fun buscarPorNombre(nombre: String) {
            recordatorioRepository.buscarPorNombre(nombre) { recordatorio, error ->
                if (recordatorio != null) {
                    _recordatorioLiveData.postValue(recordatorio)
                } else {
                    _errorLiveData.postValue(error)
                }
            }
        }
}