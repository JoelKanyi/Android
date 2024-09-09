package com.android.swingmusic.album.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.album.domain.AlbumRepository
import com.android.swingmusic.album.presentation.event.AlbumWithInfoUiEvent
import com.android.swingmusic.album.presentation.state.AlbumInfoWithGroupedTracks
import com.android.swingmusic.album.presentation.state.AlbumWithInfoState
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumWithInfoViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _albumWithInfoState: MutableState<AlbumWithInfoState> =
        mutableStateOf(AlbumWithInfoState())
    val albumWithInfoState: State<AlbumWithInfoState> get() = _albumWithInfoState

    // TODO: handle this screen specific events

    private fun updateAlbumInfoState(resource: Resource<AlbumWithInfo>) {
        when (resource) {
            is Resource.Success -> {
                val groupedTracks = resource.data?.tracks
                    ?.sortedBy { track -> track.trackNumber }
                    ?.groupBy { track -> track.disc }
                    ?.toSortedMap()

                val orderedTracks = groupedTracks?.values?.flatten() ?: emptyList()

                _albumWithInfoState.value = _albumWithInfoState.value.copy(
                    orderedTracks = orderedTracks ,
                    infoResource = Resource.Success(
                        data = AlbumInfoWithGroupedTracks(
                            albumInfo = resource.data?.albumInfo,
                            groupedTracks = groupedTracks ?: emptyMap()
                        )
                    )
                )
            }

            is Resource.Loading -> {
                _albumWithInfoState.value =
                    _albumWithInfoState.value.copy(infoResource = Resource.Loading())
            }

            else -> {
                _albumWithInfoState.value =
                    _albumWithInfoState.value.copy(
                        infoResource = Resource.Error(
                            message = resource.message!!
                        )
                    )
            }
        }
    }

    fun onAlbumWithInfoUiEvent(event: AlbumWithInfoUiEvent) {
        when (event) {
            is AlbumWithInfoUiEvent.OnLoadAlbumWithInfo -> {
                viewModelScope.launch {
                    _albumWithInfoState.value =
                        _albumWithInfoState.value.copy(albumHash = event.albumHash)

                    val result = albumRepository.getAlbumWithInfo(event.albumHash)
                    result.collectLatest {
                        updateAlbumInfoState(it)
                    }
                }
            }

            is AlbumWithInfoUiEvent.OnRefreshAlbumInfo -> {
                viewModelScope.launch {
                    val result =
                        albumRepository.getAlbumWithInfo(_albumWithInfoState.value.albumHash ?: "")
                    result.collectLatest {
                        updateAlbumInfoState(it)
                    }
                }
            }

            else -> {}
        }
    }
}