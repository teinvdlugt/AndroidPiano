package com.teinvdlugt.android.piano;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter mAdapter;
    DatabaseReference mSongsRef;

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
        Log.d("hi", "onCreate: hi");
        mSongsRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(authId)
                .child(Database.SONGS);
        mAdapter = new FirebaseRecyclerAdapter<Song, SongViewHolder>(Song.class, R.layout.list_item_song,
                SongViewHolder.class, mSongsRef) {
            @Override
            protected void populateViewHolder(SongViewHolder viewHolder, Song model, int position) {
                viewHolder.bind(MainActivity.this, model, mAdapter.getRef(position).getKey());
            }
        }; // TODO: 11-2-17 Use FirebaseIndexRecyclerAdapter, see FirebaseUI docs on GitHub
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    public void onClickAddSong(View view) {
        String newKey = mSongsRef.push().getKey();
        SongActivity.openActivity(this, new Song(newKey));
    }
}
