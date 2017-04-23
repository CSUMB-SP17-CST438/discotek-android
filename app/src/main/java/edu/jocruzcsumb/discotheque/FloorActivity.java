package edu.jocruzcsumb.discotheque;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static edu.jocruzcsumb.discotheque.FloorService.EVENT_FLOOR_JOINED;
import static edu.jocruzcsumb.discotheque.FloorService.EVENT_LEAVE_FLOOR;
import static edu.jocruzcsumb.discotheque.SeamlessMediaPlayer.EVENT_SONG_STARTED;
import static edu.jocruzcsumb.discotheque.SeamlessMediaPlayer.EVENT_SONG_STOPPED;

public class FloorActivity extends AppCompatActivity
{

    private static final String TAG = "FloorActivity";
    private static final String CURRENT_TAB_TAG = "current_tab";

    public Floor floor = null;
    private ImageView albumCoverView;
    private TextView songInfoView;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Song currentSong;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);

        // This tells the activity what LocalBroadcast Events to listen for
        IntentFilter f = new IntentFilter();
        f.addAction(EVENT_FLOOR_JOINED);
//        f.addAction(EVENT_SONG_LIST_UPDATE);
//        f.addAction(EVENT_USER_LIST_UPDATE);
//        f.addAction(EVENT_MESSAGE_LIST_UPDATE);
//        f.addAction(EVENT_USER_ADD);
//        f.addAction(EVENT_USER_REMOVE);
//        f.addAction(EVENT_MESSAGE_ADD);
        f.addAction(EVENT_SONG_STARTED);
        f.addAction(EVENT_SONG_STOPPED);

        FloorListener listener = new FloorListener(f, this, TAG)
        {
            @Override
            public void onFloorJoined(Floor floor)
            {
                FloorActivity.this.floor = floor;
                FloorActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });

            }

            public void onSongStarted(Song x)
            {
                final Song s = x;
                Log.d(TAG, EVENT_SONG_STARTED + ": " + s.getName() + " - " + s.getArtist());
                FloorActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setCurrentSong(s);
                    }
                });
            }

            public void onSongStopped(Song s)
            {
            }
        };

        // Start the floor service
        Intent i = getIntent();
        int floorId = i.getIntExtra(Floor.TAG, 0);
        if (floorId == 0)
        {
            Log.w(TAG, "No floor was passed to this activity, aborting...");
            finish();
        }
        else
        {
            FloorService.joinFloor(this, floorId);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        int t = 1;
        if (savedInstanceState != null)
        {
            t = savedInstanceState.getInt(CURRENT_TAB_TAG, 1);
        }

        mViewPager.setCurrentItem(t, false);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        albumCoverView = (ImageView) findViewById(R.id.song_artwork);
        songInfoView = (TextView) findViewById(R.id.song_title_text);

    }

    private void setCurrentSong(Song s)
    {
        currentSong = s;
        songInfoView.setText((s.getName() + " - " + s.getArtist()));
        Picasso.with(FloorActivity.this)
               .load(s.getArtworkUrl())
               .into(albumCoverView);
        //TODO progressbar
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        floor = (Floor) savedInstanceState.getParcelable(Floor.TAG);
        if (floor != null)
        {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        setCurrentSong((Song) savedInstanceState.getParcelable(Song.TAG));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        if(floor != null) savedInstanceState.putParcelable(Floor.TAG, floor);
        if(currentSong != null) savedInstanceState.putParcelable(Song.TAG, currentSong);
        if(floor != null) savedInstanceState.putInt(CURRENT_TAB_TAG, mViewPager.getCurrentItem());
    }

    @Override
    public void onBackPressed()
    {
        broadcast(EVENT_LEAVE_FLOOR);
        super.onBackPressed();
    }

    // EVENTS are broadcasted here
    private void broadcast(String event)
    {
        Intent k = new Intent(event);
        broadcast(k);
    }

    private void broadcast(String event, Parcelable params)
    {
        Intent k = new Intent(event);
        k.putExtra(event, params);
        broadcast(k);
    }

    private void broadcast(Intent k)
    {
        LocalBroadcastManager.getInstance(getApplicationContext())
                             .sendBroadcast(k);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_floor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_settings:
                break;
            case R.id.action_leave_floor:
                broadcast(EVENT_LEAVE_FLOOR);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    return ChatFragment.newInstance();
                case 1:
                    return SongFragment.newInstance();
                case 2:
                    return UserFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount()
        {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return getString(R.string.chat_fragment_title);
                case 1:
                    return getString(R.string.song_fragment_title);
                case 2:
                    return getString(R.string.user_fragment_title);
            }
            return null;
        }
    }
}
