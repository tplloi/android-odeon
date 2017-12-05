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

package fr.nihilus.music.ui.artists;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import java.util.ArrayList;
import java.util.List;

import fr.nihilus.music.R;
import fr.nihilus.music.glide.GlideApp;
import fr.nihilus.music.media.MediaItems;
import fr.nihilus.music.utils.MediaID;
import fr.nihilus.music.utils.MediaItemDiffCallback;

class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistHolder> {

    private final Fragment mFragment;
    private final List<MediaItem> mItems = new ArrayList<>();
    private final RequestBuilder<Bitmap> mGlide;
    private OnArtistSelectedListener mListener;

    ArtistAdapter(@NonNull Fragment fragment) {
        mFragment = fragment;
        Drawable dummyCover = AppCompatResources.getDrawable(fragment.getContext(),
                R.drawable.ic_person_24dp);
        mGlide = GlideApp.with(fragment).asBitmap()
                .error(dummyCover)
                .centerCrop();
    }

    @Override
    public ArtistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_grid_item, parent, false);
        return new ArtistHolder(v);
    }

    @Override
    public void onBindViewHolder(final ArtistHolder holder, int position) {
        final MediaItem artist = mItems.get(position);
        holder.artistName.setText(artist.getDescription().getTitle());
        mGlide.load(artist.getDescription().getIconUri()).into(holder.cover);

        Bundle extras = artist.getDescription().getExtras();
        if (extras != null) {
            int trackCount = extras.getInt(MediaItems.EXTRA_NUMBER_OF_TRACKS);
            String subtitle = holder.subtitle.getResources()
                    .getQuantityString(R.plurals.number_of_tracks, trackCount, trackCount);
            holder.subtitle.setText(subtitle);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onArtistSelected(holder, mItems.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public long getItemId(int position) {
        if (mItems != null && hasStableIds()) {
            String mediaId = mItems.get(position).getMediaId();
            return Long.parseLong(MediaID.extractMusicID(mediaId));
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @Override
    public void onViewRecycled(ArtistHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(mFragment).clear(holder.cover);
    }

    void setOnArtistSelectedListener(OnArtistSelectedListener listener) {
        mListener = listener;
    }

    void updateArtists(final List<MediaItem> artists) {
        if (artists.isEmpty() && mItems.isEmpty()) {
            // Dispatch a general change notification to update RecyclerFragment's empty state
            notifyDataSetChanged();
        } else {
            // Calculate diff and dispatch individual change notifications
            MediaItemDiffCallback diffCallback = new MediaItemDiffCallback(mItems, artists);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffCallback, false);
            mItems.clear();
            mItems.addAll(artists);
            result.dispatchUpdatesTo(this);
        }
    }

    interface OnArtistSelectedListener {
        void onArtistSelected(ArtistHolder holder, MediaItem artist);
    }

    static class ArtistHolder extends RecyclerView.ViewHolder {
        final TextView artistName;
        final TextView subtitle;
        final ImageView cover;

        ArtistHolder(View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artistName);
            subtitle = itemView.findViewById(R.id.subtitle);
            cover = itemView.findViewById(R.id.cover);
        }
    }
}
