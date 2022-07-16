/*
 * Copyright 2020 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nihilus.music.ui.cleanup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.nihilus.music.core.media.MediaId
import fr.nihilus.music.core.media.MediaId.Builder.CATEGORY_ALL
import fr.nihilus.music.core.media.MediaId.Builder.TYPE_TRACKS
import fr.nihilus.music.core.ui.actions.DeleteTracksAction
import fr.nihilus.music.media.tracks.DeleteTracksResult
import fr.nihilus.music.media.usage.UsageManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CleanupViewModel @Inject constructor(
    private val deleteTracks: DeleteTracksAction,
    usageManager: UsageManager
) : ViewModel() {

    private val pendingEvent = MutableStateFlow<DeleteTracksResult?>(null)
    private val selection = MutableStateFlow<Set<MediaId>>(emptySet())

    val state: LiveData<CleanupState> = combine(
        usageManager.getDisposableTracks()
            .map { tracks ->
                tracks.map {
                    CleanupState.Track(
                        id = MediaId(TYPE_TRACKS, CATEGORY_ALL, it.trackId),
                        title = it.title,
                        fileSizeBytes = it.fileSizeBytes,
                        lastPlayedTime = it.lastPlayedTime,
                        selected = false
                    )
                }
            }
            .onStart { emit(emptyList()) },
        selection,
        pendingEvent,
    ) { tracks, selection, event ->
        val candidates = tracks.map {
            when (it.id) {
                in selection -> it.copy(selected = true)
                else -> it
            }
        }
        val selectedTracks = candidates.filter { it.selected }
        CleanupState(
            tracks = candidates,
            selectedCount = selectedTracks.size,
            selectedFreedBytes = selectedTracks.sumOf { it.fileSizeBytes },
            result = event
        )
    }
        .asLiveData()

    fun deleteSelected() {
        viewModelScope.launch {
            val selectedIds = selection.value
            if (selectedIds.isNotEmpty()) {
                val result = deleteTracks(selectedIds.toList())
                if (result is DeleteTracksResult.Deleted) {
                    clearSelection()
                }
                pendingEvent.value = result
            }
        }
    }

    fun toggleSelection(trackId: MediaId) {
        selection.update { selection ->
            if (trackId in selection) {
                selection - trackId
            } else {
                selection + trackId
            }
        }
    }

    fun clearSelection() {
        selection.value = emptySet()
    }

    fun acknowledgeResult() {
        pendingEvent.value = null
    }
}
