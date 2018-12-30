/*
 * Copyright 2018 Thibault Seisel
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

package fr.nihilus.music.library.songs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import fr.nihilus.music.R
import fr.nihilus.music.client.MediaBrowserConnection
import fr.nihilus.music.client.MediaListViewModel
import fr.nihilus.music.media.CATEGORY_MUSIC
import fr.nihilus.music.media.command.DeleteTracksCommand
import fr.nihilus.music.media.musicIdFrom
import fr.nihilus.music.utils.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongListViewModel
@Inject constructor(
    private val context: Context,
    connection: MediaBrowserConnection
) : MediaListViewModel(connection) {

    private val _toastMessage = MutableLiveData<Event<CharSequence>>()
    val toastMessage: LiveData<Event<CharSequence>>
        get() = _toastMessage

    fun playAllShuffled() {
        launch {
            connection.setShuffleModeEnabled(true)
            connection.playFromMediaId(CATEGORY_MUSIC)
        }
    }

    fun onSongSelected(song: MediaBrowserCompat.MediaItem) {
        launch {
            connection.playFromMediaId(song.mediaId!!)
        }
    }

    fun deleteSongs(songsToDelete: List<MediaBrowserCompat.MediaItem>) {
        launch {
            val songIds = LongArray(songsToDelete.size) { position ->
                val mediaItem = songsToDelete[position]
                musicIdFrom(mediaItem.mediaId)!!.toLong()
            }

            val parameters = Bundle(1)
            parameters.putLongArray(DeleteTracksCommand.PARAM_TRACK_IDS, songIds)
            val result = connection.sendCommand(DeleteTracksCommand.CMD_NAME, parameters)

            if (result.resultCode == R.id.abc_result_success) {
                val deletedTracksCount = result.resultData?.getInt(DeleteTracksCommand.RESULT_DELETE_COUNT) ?: 0
                val successMessage = context.resources.getQuantityString(
                    R.plurals.deleted_songs_confirmation,
                    deletedTracksCount,
                    deletedTracksCount
                )
                _toastMessage.value = Event(successMessage)
            }
        }
    }
}