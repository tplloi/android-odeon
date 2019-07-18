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

package fr.nihilus.music.media.usage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface MediaUsageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun recordEvent(usageEvent: MediaUsageEvent)

    @Deprecated("Events are only recorded on a a time.")
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun recordUsageEvents(events: Iterable<MediaUsageEvent>)

    @Deprecated("This function has no use.")
    @Query("DELETE FROM usage_event WHERE event_time < :timeThreshold")
    suspend fun deleteAllEventsBefore(timeThreshold: Long)

    @Query("SELECT track_id, COUNT(*) AS event_count, MAX(event_time) AS last_event_time FROM usage_event GROUP BY track_id")
    suspend fun getTracksUsage(): List<TrackUsage>

    @Query("SELECT track_id, COUNT(*) AS event_count FROM usage_event GROUP BY track_id ORDER BY COUNT(*) DESC LIMIT :limit")
    suspend fun getMostRatedTracks(limit: Int): List<TrackScore>

    @Query("DELETE FROM usage_event WHERE track_id IN (:trackIds)")
    suspend fun deleteEventsForTracks(trackIds: LongArray)
}