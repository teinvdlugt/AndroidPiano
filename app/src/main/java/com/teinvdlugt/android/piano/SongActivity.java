package com.teinvdlugt.android.piano;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SongActivity extends AppCompatActivity {
    public static final String SONG_ID = "song_id";

    private Song mSong;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

    private ViewSwitcher titleVS;
    private ViewSwitcher composerVS;
    private ViewSwitcher opusVS;
    private ViewSwitcher descriptionVS;
    private TextView titleTV;
    private TextView composerTV;
    private TextView opusTV;
    private TextView descriptionTV;
    private EditText titleET;
    private EditText composerET;
    private EditText opusET;
    private EditText descriptionET;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        String songId = getIntent().getStringExtra(SONG_ID);
        mRef = FirebaseDatabase.getInstance().getReference()
                .child(Database.USERS)
                .child("DEBUG")
                .child(Database.SONGS)
                .child(songId);
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // Song was probably removed
                    finish();
                }
                mSong = dataSnapshot.getValue(Song.class);
                loadSong();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SongActivity.this, "Something failed", Toast.LENGTH_SHORT).show();
                // TODO: 11-2-17 Remove before publishing
            }
        };
        mRef.addValueEventListener(mValueListener);
    }

    private void initViews() {
        titleVS = (ViewSwitcher) findViewById(R.id.title_switcher);
        composerVS = (ViewSwitcher) findViewById(R.id.composer_switcher);
        opusVS = (ViewSwitcher) findViewById(R.id.opus_switcher);
        descriptionVS = (ViewSwitcher) findViewById(R.id.description_switcher);
        titleTV = (TextView) findViewById(R.id.title_textView);
        composerTV = (TextView) findViewById(R.id.composer_textView);
        opusTV = (TextView) findViewById(R.id.opus_textView);
        descriptionTV = (TextView) findViewById(R.id.description_textView);
        titleET = (EditText) findViewById(R.id.title_editText);
        composerET = (EditText) findViewById(R.id.composer_editText);
        opusET = (EditText) findViewById(R.id.opus_editText);
        descriptionET = (EditText) findViewById(R.id.description_editText);
    }

    private void loadSong() {
        titleTV.setText(mSong.getTitle());
        composerTV.setText(mSong.getComposer());
        opusTV.setText(mSong.getOpus());
        descriptionTV.setText(mSong.getDescription());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mRef.removeEventListener(mValueListener);
        super.onDestroy();
    }
}
