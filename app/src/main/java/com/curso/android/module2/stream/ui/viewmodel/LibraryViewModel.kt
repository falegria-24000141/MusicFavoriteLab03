package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.curso.android.module2.stream.data.model.Playlist
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.curso.android.module2.stream.data.model.Song // Importamos Song


/**
 * ================================================================================
 * LIBRARY VIEW MODEL - Lógica de la Biblioteca del Usuario
 * ================================================================================
 *
 * ViewModel para la pantalla Library que muestra las playlists del usuario.
 *
 * PATRÓN MVVM APLICADO:
 * - Expone el estado de las playlists como StateFlow
 * - La UI observa el estado y se recompone automáticamente
 * - Sigue el mismo patrón que HomeViewModel y SearchViewModel
 *
 * COMPARACIÓN CON TABS:
 * --------------------
 * Este ViewModel es parte del sistema de BottomNavigation.
 * Cada tab (Home, Search, Library) tiene su propio ViewModel
 * que mantiene su estado independiente.
 */


/**
 * Estado de la pantalla Library.
 */

sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    /**
     * Datos cargados exitosamente.
     *
     * @property playlists Lista de playlists del usuario
     * @property favoriteSongs Lista de canciones marcadas como favoritas
     */
    data class Success(
        val playlists: List<Playlist>,
        val favoriteSongs: List<Song> // Agregamos este campo
    ) : LibraryUiState

    data class Error(
        val message: String
    ) : LibraryUiState
}

class LibraryViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibraryData()
    }

    /**
     * Carga los datos de la biblioteca (Playlists y Favoritos).
     */
    private fun loadLibraryData() {
        _uiState.value = LibraryUiState.Loading

        // 1. ESTA ES LA CLAVE: Hay que pedir las canciones y filtrarlas
        val allSongs = repository.getAllSongs()
        val favorites = allSongs.filter { it.isFavorite }

        // 2. Pedir las playlists (Enviamos lista vacía para limpiar la UI y dejar solo favoritos)
        val playlists = emptyList<Playlist>()

        // 3. Pasar AMBAS listas al Success
        _uiState.value = LibraryUiState.Success(
            playlists = playlists,
            favoriteSongs = favorites // Si esto es una lista vacía, no se verá nada
        )
    }

    /**
     * Recarga los datos de la biblioteca.
     */
    fun refresh() {
        loadLibraryData()
    }
}