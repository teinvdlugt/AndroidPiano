package com.teinvdlugt.android.piano;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String SORT_BY_PREF = "sort_by";
    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_COMPOSER = 1;

    private RecyclerAdapter mAdapter;
    private DatabaseReference mSongsRef;
    private ValueEventListener eventListener;

    private ValueEventListener sortBySongEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Song> songs = Song.getSongsList(dataSnapshot);
            mAdapter.setData(songs);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };
    private ValueEventListener sortByComposerEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Song> songs = Song.getSongsList(dataSnapshot);
            mAdapter.setData(Composer.getComposers(songs));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        // TODO Maybe show when app is offline?
        // https://firebase.google.com/docs/database/android/offline-capabilities#section-connection-state

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // TODO: 11-2-17 Why?
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerAdapter(this);

        String authId = "DEBUG";
        mSongsRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(authId)
                .child(Database.SONGS);

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SORT_BY_PREF, SORT_BY_TITLE) == SORT_BY_TITLE)
            eventListener = sortBySongEventListener;
        else eventListener = sortByComposerEventListener;
        mSongsRef.orderByChild(Database.TITLE).addValueEventListener(eventListener);

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSongsRef.removeEventListener(eventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort_by_composer_menu_action) {
            item.setChecked(!item.isChecked());
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(SORT_BY_PREF, item.isChecked() ? SORT_BY_COMPOSER : SORT_BY_TITLE)
                    .apply();
            mSongsRef.removeEventListener(eventListener);
            eventListener = item.isChecked() ? sortByComposerEventListener : sortBySongEventListener;
            mSongsRef.addValueEventListener(eventListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.sort_by_composer_menu_action)
                .setChecked(PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(SORT_BY_PREF, SORT_BY_TITLE) == SORT_BY_COMPOSER);
        return true;
    }

    public void onClickAddSong(View view) {
        Song song = new Song();
        String newKey = mSongsRef.push().getKey();
        mSongsRef.child(newKey).setValue(song);
        song.setKey(newKey);
        SongActivity.openActivity(this, song);
    }
}
