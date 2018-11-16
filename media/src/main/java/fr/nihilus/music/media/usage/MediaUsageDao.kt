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

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
internal interface MediaUsageDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun recordUsageEvents(events: Iterable<MediaUsageEvent>)

    @Query("DELETE FROM usage_event WHERE event_time < :timeThreshold")
    fun deleteAllEventsBefore(timeThreshold: Long)

    @Query("SELECT track_id, COUNT(*) AS event_count FROM usage_event GROUP BY track_id ORDER BY COUNT(*) DESC LIMIT :limit")
    fun getMostRatedTracks(limit: Int): List<TrackScore>

    @Query("SELECT track_id, COUNT(*) AS event_count FROM usage_event GROUP BY track_id ORDER BY COUNT(*) ASC")
    fun getRatedTracks(): List<TrackScore>
}