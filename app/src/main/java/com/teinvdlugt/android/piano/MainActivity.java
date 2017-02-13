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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    public static final String SORT_BY_PREF = "sort_by";
    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_COMPOSER = 1;

    private FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference mSongsRef;

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

        String authId = "DEBUG";
        mSongsRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(authId)
                .child(Database.SONGS);

        int sort_by = PreferenceManager.getDefaultSharedPreferences(this).getInt(SORT_BY_PREF, 0);
        switch (sort_by) {
            case SORT_BY_TITLE:
                Query query = mSongsRef.orderByChild(Database.TITLE);
                mAdapter = new FirebaseRecyclerAdapter<Song, SongViewHolder>(Song.class, R.layout.list_item_song,
                        SongViewHolder.class, query) {
                    @Override
                    protected void populateViewHolder(SongViewHolder viewHolder, Song model, int position) {
                        viewHolder.bind(MainActivity.this, model, mAdapter.getRef(position).getKey());
                    }
                }; // TODO: 11-2-17 Use FirebaseIndexRecyclerAdapter, see FirebaseUI docs on GitHub
                break;
            case SORT_BY_COMPOSER:
                Query composers = Database.getDatabaseInstance().getReference()
                        .child(Database.USERS)
                        .child(authId)
                        .child(Database.PEOPLE)
                        .orderByKey();
                composers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Toast.makeText(MainActivity.this, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mAdapter = new FirebaseRecyclerAdapter<Composer, ComposerViewHolder>(Composer.class,
                        R.layout.list_item_composer, ComposerViewHolder.class, composers) {
                    @Override
                    protected void populateViewHolder(ComposerViewHolder viewHolder, Composer model, int position) {
                        viewHolder.bind(mAdapter.getRef(position).getKey(), mSongsRef);
                    }
                };
        }

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sort_by_composer_menu_action) {
            item.setChecked(!item.isChecked());
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(SORT_BY_PREF, item.isChecked() ? SORT_BY_COMPOSER : SORT_BY_TITLE)
                    .apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
