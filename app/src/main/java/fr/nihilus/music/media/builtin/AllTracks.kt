/*
 * Copyright 2017 Thibault Seisel
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

package fr.nihilus.music.media.builtin

import android.content.Context
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import fr.nihilus.music.R
import fr.nihilus.music.asMediaDescription
import fr.nihilus.music.media.cache.MusicCache
import fr.nihilus.music.media.source.MusicDao
import fr.nihilus.music.utils.MediaID
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

internal class AllTracks
@Inject constructor(
        private val context: Context,
        private val musicDao: MusicDao,
        private val cache: MusicCache
) : BuiltinItem {

    override fun asMediaItem(): MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaID.ID_MUSIC)
                .setTitle(context.getString(R.string.all_music))
                .build()
        return MediaItem(description, MediaItem.FLAG_BROWSABLE or MediaItem.FLAG_PLAYABLE)
    }

    override fun getChildren(parentMediaId: String): Single<List<MediaItem>> {
        val builder = MediaDescriptionCompat.Builder()
        return musicDao.getAllTracks()
                .flatMap { Observable.fromIterable(it) }
                .doOnNext { metadata ->
                    val musicId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    cache.putMetadata(musicId, metadata)
                }
                .map { metadata ->
                    val description = metadata.asMediaDescription(builder, MediaID.ID_MUSIC)
                    MediaItem(description, MediaItem.FLAG_PLAYABLE)
                }.toList()
    }
}