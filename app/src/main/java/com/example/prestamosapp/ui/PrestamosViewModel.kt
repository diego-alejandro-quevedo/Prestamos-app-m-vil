package com.example.prestamosapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamosapp.data.ApiResult
import com.example.prestamosapp.data.PrestamosRepository
import com.example.prestamosapp.data.model.Herramienta
import com.example.prestamosapp.data.model.HerramientaRequest
import com.example.prestamosapp.data.model.Prestamo
import com.example.prestamosapp.data.model.PrestamoRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Resultado de la búsqueda de un préstamo por ID. */
sealed interface BusquedaState {
    object Idle : BusquedaState
    object Loading : BusquedaState
    data class Found(val id: Long, val prestamo: Prestamo) : BusquedaState
    data class NotFound(val message: String) : BusquedaState
}

data class UiState(
    val herramientas: List<Herramienta> = emptyList(),
    val prestamos: List<Prestamo> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val busqueda: BusquedaState = BusquedaState.Idle,
)

class PrestamosViewModel(
    private val repository: PrestamosRepository = PrestamosRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // Mensajes de un solo uso (errores / confirmaciones) para el Snackbar
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val herramientas = async { repository.herramientas() }
            val prestamos = async { repository.prestamos() }

            when (val r = herramientas.await()) {
                is ApiResult.Ok -> _state.update { it.copy(herramientas = r.data) }
                is ApiResult.Fail -> _events.tryEmit("Herramientas: ${r.message}")
            }
            when (val r = prestamos.await()) {
                is ApiResult.Ok -> _state.update { it.copy(prestamos = r.data) }
                is ApiResult.Fail -> _events.tryEmit("Préstamos: ${r.message}")
            }
            _state.update { it.copy(loading = false) }
        }
    }

    /** Busca un préstamo con GET api/Prestamos/listar/{id}. */
    fun buscarPrestamo(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(busqueda = BusquedaState.Loading) }
            val resultado: BusquedaState = when (val r = repository.prestamoPorId(id)) {
                is ApiResult.Ok -> BusquedaState.Found(id, r.data)
                is ApiResult.Fail -> BusquedaState.NotFound(r.message)
            }
            _state.update { it.copy(busqueda = resultado) }
        }
    }

    fun limpiarBusqueda() {
        _state.update { it.copy(busqueda = BusquedaState.Idle) }
    }

    fun crearHerramienta(request: HerramientaRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val r = repository.crearHerramienta(request)) {
                is ApiResult.Ok -> {
                    _events.tryEmit("Herramienta creada")
                    onSuccess()
                    refresh()
                }
                is ApiResult.Fail -> _events.tryEmit(r.message)
            }
            _state.update { it.copy(saving = false) }
        }
    }

    fun crearPrestamo(request: PrestamoRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val r = repository.crearPrestamo(request)) {
                is ApiResult.Ok -> {
                    _events.tryEmit("Préstamo registrado")
                    onSuccess()
                    refresh()
                }
                is ApiResult.Fail -> _events.tryEmit(r.message)
            }
            _state.update { it.copy(saving = false) }
        }
    }
}