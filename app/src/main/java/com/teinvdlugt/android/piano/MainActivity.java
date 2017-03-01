package com.teinvdlugt.android.piano;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, RecyclerAdapter.OnSongClickListener {
    private static final String TAG = "MainActivity"; // Debugging

    public static final String SORT_BY_PREF = "sort_by";
    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_COMPOSER = 1;

    private FirebaseAuth mAuth;

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

        // Deal with Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Deal with Firebase Database and RecyclerAdapter
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // TODO: 11-2-17 Why?
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerAdapter(this, this);

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid() + ":" + user.getDisplayName());
            retrieveSongs(user.getUid());
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
    }

    private void retrieveSongs(String authId) {
        if (mSongsRef != null && eventListener != null)
            mSongsRef.removeEventListener(eventListener);
        mSongsRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(authId)
                .child(Database.SONGS);
        mSongsRef.orderByChild(Database.TITLE).addValueEventListener(eventListener);
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
            case R.id.signOut_menuAction:
                mAuth.signOut();
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
        // Check composer box (or not)
        menu.findItem(R.id.sort_by_composer_menu_action)
                .setChecked(PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(SORT_BY_PREF, SORT_BY_TITLE) == SORT_BY_COMPOSER);

        // Deal with SearchView
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_menuAction));
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFilter.setSearchQuery(newText.trim());
                resetAdapterSongs();
                return true;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mFilter.getSearchQuery() != null && !mFilter.getSearchQuery().isEmpty()) {
            // Hide soft keyboard
            View focus = getCurrentFocus();
            if (focus != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            invalidateOptionsMenu();
            mFilter.setSearchQuery(null);
            resetAdapterSongs();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClickSong(Song song) {
        SongActivity.openActivity(this, song, Composer.getComposerNames(mSongs));
    }

    public void onClickAddSong(View view) {
        Song song = new Song();
        String newKey = mSongsRef.push().getKey();
        mSongsRef.child(newKey).setValue(song);
        song.setKey(newKey);
        SongActivity.openActivity(this, song, Composer.getComposerNames(mSongs));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
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
