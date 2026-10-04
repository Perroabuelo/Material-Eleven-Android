package io.github.perroabuelo.materialeleven.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.perroabuelo.materialeleven.core.library.Track
import io.github.perroabuelo.materialeleven.core.library.TrackOrder
import io.github.perroabuelo.materialeleven.data.MediaStoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SongsState {
    data object Loading : SongsState

    data class Loaded(val tracks: List<Track>) : SongsState
}

class SongsViewModel(source: MediaStoreSource) : ViewModel() {
    val state: StateFlow<SongsState> = source.tracks()
        .map<List<Track>, SongsState> { tracks -> SongsState.Loaded(tracks.sortedWith(TrackOrder.byTitle())) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SongsState.Loading)

    private companion object {
        // Keeps the query alive across configuration changes.
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
