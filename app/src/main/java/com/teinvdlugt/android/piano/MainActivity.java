package com.teinvdlugt.android.piano;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter mAdapter;
    DatabaseReference mSongsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // TODO: 11-2-17 Why?
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String authId = "AUTH_ID";
        mSongsRef = FirebaseDatabase.getInstance().getReference()
                .child(Database.USERS)
                .child(authId)
                .child(Database.SONGS);
        mAdapter = new FirebaseRecyclerAdapter<Song, SongViewHolder>(Song.class, R.layout.list_item_song,
                SongViewHolder.class, mSongsRef) {
            @Override
            protected void populateViewHolder(SongViewHolder viewHolder, Song model, int position) {
                viewHolder.bind(model);
            }
        };
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
}
