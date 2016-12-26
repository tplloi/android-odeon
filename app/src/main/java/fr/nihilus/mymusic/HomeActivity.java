package fr.nihilus.mymusic;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.util.List;

import fr.nihilus.mymusic.MediaBrowserFragment.ConnectedCallback;
import fr.nihilus.mymusic.palette.PaletteBitmap;
import fr.nihilus.mymusic.palette.PaletteBitmapTranscoder;
import fr.nihilus.mymusic.settings.SettingsActivity;
import fr.nihilus.mymusic.ui.albums.AlbumGridFragment;
import fr.nihilus.mymusic.ui.artists.ArtistsFragment;
import fr.nihilus.mymusic.ui.songs.SongListFragment;
import fr.nihilus.mymusic.utils.MediaID;
import fr.nihilus.mymusic.utils.PermissionUtil;
import fr.nihilus.mymusic.view.PlayerView;

@SuppressWarnings("ConstantConditions")
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_SETTINGS = 42;
    public static final String ACTION_ALBUMS = "fr.nihilus.mymusic.ACTION_ALBUMS";
    private static final String TAG = "HomeActivity";
    private static final String KEY_DAILY_SONG = "daily_song";
    public static final String KEY_BOTTOMSHEET_STATE = "bottomsheet_state";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private PlayerView mPlayerView;
    private MediaItem mDaily;

    private final ConnectedCallback mConnectionCallback = new ConnectedCallback() {
        @Override
        public void onConnected() {
            MediaControllerCompat controller = MediaControllerCompat
                    .getMediaController(HomeActivity.this);
            mPlayerView.setMediaController(controller);
        }
    };

    private final SubscriptionCallback mSubscriptionCallback = new SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
            if (children.size() > 0) {
                mDaily = children.get(0);
                prepareHeaderView(mDaily);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupNavigationDrawer();
        setupPlayerView();

        if (savedInstanceState == null) {
            if (PermissionUtil.hasExternalStoragePermission(this)) {
                loadFirstFragment();
                loadDailySong();
            } else PermissionUtil.requestExternalStoragePermission(this);
        } else {
            mDaily = savedInstanceState.getParcelable(KEY_DAILY_SONG);
            prepareHeaderView(mDaily);
            final int bottomSheetState = savedInstanceState.getInt(KEY_BOTTOMSHEET_STATE,
                    BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(mPlayerView).setState(bottomSheetState);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        mPlayerView.setMediaController(null);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_DAILY_SONG, mDaily);
        outState.putInt(KEY_BOTTOMSHEET_STATE, BottomSheetBehavior.from(mPlayerView).getState());
    }

    private void setupPlayerView() {
        mPlayerView = (PlayerView) findViewById(R.id.playerView);
        MediaBrowserFragment.getInstance(getSupportFragmentManager())
                .doWhenConnected(mConnectionCallback);
        BottomSheetBehavior.from(mPlayerView)
                .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        mPlayerView.setHeaderOpacity(1 - slideOffset);
                    }
                });
    }

    private void loadFirstFragment() {
        Fragment firstFragment = new SongListFragment();
        @IdRes int checkedItemId = R.id.action_all;
        String callingAction = getIntent().getAction();

        if (callingAction != null) {
            switch (callingAction) {
                case ACTION_ALBUMS:
                    firstFragment = new AlbumGridFragment();
                    checkedItemId = R.id.action_albums;
                    break;
            }
        }

        mNavigationView.setCheckedItem(checkedItemId);
        swapFragment(firstFragment);
    }

    private void loadDailySong() {
        MediaBrowserFragment.getInstance(getSupportFragmentManager())
                .subscribe(MediaID.ID_DAILY, mSubscriptionCallback);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_opened, R.string.drawer_closed);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = (NavigationView) findViewById(R.id.navDrawer);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        onOptionsItemSelected(item);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_all:
                swapFragment(new SongListFragment());
                return true;
            case R.id.action_albums:
                swapFragment(new AlbumGridFragment());
                return true;
            case R.id.action_artists:
                swapFragment(new ArtistsFragment());
                return true;
            case R.id.action_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsActivity, REQUEST_SETTINGS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_SETTINGS) && (resultCode == RESULT_OK)) {
            recreate();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        super.onNewIntent(intent);
        // Peut peut-être recevoir les actions des raccourcis ?
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.EXTERNAL_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) loadDailySong();
            loadFirstFragment();
        }
    }

    private void swapFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newFrag)
                .commit();
    }

    private void prepareHeaderView(final MediaItem daily) {
        if (daily != null) {
            Uri artUri = daily.getDescription().getIconUri();
            CharSequence title = daily.getDescription().getTitle();
            CharSequence subtitle = daily.getDescription().getSubtitle();

            View header = mNavigationView.getHeaderView(0);
            final View band = header.findViewById(R.id.band);
            final TextView titleText = ((TextView) header.findViewById(R.id.title));
            final TextView subtitleText = ((TextView) header.findViewById(R.id.subtitle));
            ImageView albumArtView = (ImageView) header.findViewById(R.id.cover);

            titleText.setText(title);
            subtitleText.setText(subtitle);

            final Drawable dummyAlbumArt = ContextCompat.getDrawable(HomeActivity.this,
                    R.drawable.dummy_album_art);

            Glide.with(HomeActivity.this).fromUri()
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(HomeActivity.this), PaletteBitmap.class)
                    .centerCrop()
                    .load(artUri)
                    .error(dummyAlbumArt)
                    .centerCrop()
                    .into(new ImageViewTarget<PaletteBitmap>(albumArtView) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            super.view.setImageBitmap(resource.bitmap);
                            Palette.Swatch swatch = resource.palette.getDominantSwatch();
                            if (swatch != null) {
                                band.setBackgroundColor(swatch.getRgb());
                                titleText.setTextColor(swatch.getBodyTextColor());
                                subtitleText.setTextColor(swatch.getBodyTextColor());
                            }
                        }
                    });

            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MediaControllerCompat controller = MediaControllerCompat
                            .getMediaController(HomeActivity.this);
                    if (controller != null) {
                        controller.getTransportControls().playFromMediaId(daily.getMediaId(), null);
                    }
                }
            });
        }
    }
}
