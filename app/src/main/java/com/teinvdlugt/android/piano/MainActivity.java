package com.teinvdlugt.android.piano;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

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
    private List<Song> mSongs;  // This List contains all songs, no matter the filter

    private Filter mFilter = new Filter();
    private Sorter mSorter = new Sorter();

    private ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mSongs = Song.getSongsList(dataSnapshot);
            resetAdapterSongs();
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
        mSongsRef.orderByChild(Database.TITLE).addValueEventListener(eventListener);

        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Resets the data of the adapter, according to the filter and
     * the sorting method.
     */
    private void resetAdapterSongs() {
        List<Song> filteredAndSorted =
                mSorter.sort(mFilter.filter(mSongs));
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SORT_BY_PREF, SORT_BY_TITLE) == SORT_BY_TITLE) {
            mAdapter.setData(filteredAndSorted);
        } else {
            mAdapter.setData(Composer.getComposers(mFilter.filter(mSongs)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSongsRef.removeEventListener(eventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_composer_menu_action:
                item.setChecked(!item.isChecked());
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putInt(SORT_BY_PREF, item.isChecked() ? SORT_BY_COMPOSER : SORT_BY_TITLE)
                        .apply();
                resetAdapterSongs();
                return true;
            case R.id.filter_menuAction:
                onClickFilter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_dialogTitle);
        builder.setView(R.layout.layout_filter_dialog);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFilter.setWishList(((CheckBox) ((AlertDialog) dialog)
                        .findViewById(R.id.wishList_checkBox)).isChecked());
                mFilter.setStarred(((CheckBox) ((AlertDialog) dialog)
                        .findViewById(R.id.starred_checkBox)).isChecked());
                resetAdapterSongs();
            }
        });
        AlertDialog dialog = builder.show();
        ((CheckBox) dialog.findViewById(R.id.wishList_checkBox)).setChecked(mFilter.getWishList());
        ((CheckBox) dialog.findViewById(R.id.starred_checkBox)).setChecked(mFilter.getStarred());
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

    // Saving and restoring the filter state across lifecycles
    public static final String FILTER = "filter";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(FILTER, mFilter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFilter = (Filter) savedInstanceState.getSerializable(FILTER);
    }
}
