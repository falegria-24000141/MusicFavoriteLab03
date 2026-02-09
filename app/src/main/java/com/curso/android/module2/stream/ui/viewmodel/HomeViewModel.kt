package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.curso.android.module2.stream.data.model.Category
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ================================================================================
 * HOME VIEW MODEL - Lógica de Presentación
 * ================================================================================
 *
 * PATRÓN MVVM (Model-View-ViewModel)
 * ----------------------------------
 * MVVM separa la aplicación en tres capas:
 *
 * 1. MODEL (data/):
 * - Datos y lógica de negocio
 * - Repository, Data Sources, Entities
 * - No conoce la UI
 *
 * 2. VIEW (ui/screens/):
 * - Composables que renderizan la UI
 * - Observa el estado del ViewModel
 * - Envía eventos de usuario al ViewModel
 * - NO contiene lógica de negocio
 *
 * 3. VIEWMODEL (ui/viewmodel/):
 * - Puente entre Model y View
 * - Expone estado observable (StateFlow)
 * - Procesa eventos de la UI
 * - Sobrevive cambios de configuración (rotación)
 *
 * FLUJO DE DATOS (UDF - Unidirectional Data Flow):
 * ------------------------------------------------
 *
 * ┌─────────────────────────────────────────────┐
 * │                                             │
 * │    ┌──────────┐    State    ┌──────────┐   │
 * │    │ViewModel │ ──────────▶ │   View   │   │
 * │    └──────────┘             └──────────┘   │
 * │         ▲                        │         │
 * │         │       Events           │         │
 * │         └────────────────────────┘         │
 * │                                             │
 * └─────────────────────────────────────────────┘
 *
 * - STATE fluye del ViewModel a la View (UI observa StateFlow)
 * - EVENTS fluyen de la View al ViewModel (clicks, inputs, etc.)
 * - NUNCA al revés: la View no modifica el estado directamente
 */

/**
 * Estado de la pantalla Home.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val categories: List<Category>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * ViewModel para la pantalla Home.
 *
 * @param repository Repositorio de música (inyectado por Koin)
 */
class HomeViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    /**
     * STATE HOLDER (MutableStateFlow)
     * Privado para que solo el ViewModel lo modifique.
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /**
     * EXPOSED STATE (StateFlow)
     * Versión inmutable expuesta a la UI.
     */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    /**
     * Carga o refresca las categorías desde el repositorio.
     * Centralizamos la lógica aquí para evitar duplicados y errores de ambigüedad.
     */
    private fun refreshData() {
        try {
            // Obtenemos los datos actuales del repositorio
            val categories = repository.getCategories()
            // Actualizamos el estado para que la UI se redibuje con los nuevos favoritos
            _uiState.value = HomeUiState.Success(categories = categories)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido al cargar datos")
        }
    }

    /**
     * Evento disparado desde la UI cuando el usuario pulsa el icono de favorito.
     * * @param songId ID de la canción a la que se le cambia el estado de favorito
     */
    fun toggleFavorite(songId: String) {
        // 1. Notificamos al repositorio para que cambie el estado en la "fuente de verdad"
        repository.toggleFavorite(songId)

        // 2. Refrescamos la UI inmediatamente para que el corazón cambie de color en la Home
        refreshData()
    }
}