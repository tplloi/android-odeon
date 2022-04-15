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

package fr.nihilus.music.library.playlists

import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.nihilus.music.core.media.parse
import fr.nihilus.music.core.ui.LoadRequest
import fr.nihilus.music.core.ui.actions.ManagePlaylistAction
import fr.nihilus.music.core.ui.client.BrowserClient
import fr.nihilus.music.core.ui.client.MediaSubscriptionException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val client: BrowserClient,
    private val actions: ManagePlaylistAction
): ViewModel() {
    private val playlistId =
        PlaylistDetailFragmentArgs.fromSavedStateHandle(savedState).playlistId.parse()

    val playlist: LiveData<MediaItem> = liveData {
        emit(
            checkNotNull(client.getItem(playlistId)) {
                "Unable to load detail of playlist $playlistId"
            }
        )
    }

    val members: LiveData<LoadRequest<List<MediaItem>>> = client.getChildren(playlistId)
        .map<List<MediaItem>, LoadRequest<List<MediaItem>>> { LoadRequest.Success(it) }
        .onStart { emit(LoadRequest.Pending) }
        .asLiveData()

    fun deletePlaylist() {
        viewModelScope.launch {
            actions.deletePlaylist(playlistId)
        }
    }
}
