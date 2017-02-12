package com.teinvdlugt.android.piano;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SongActivity extends AppCompatActivity {
    public static final String SONG_EXTRA = "song";

    private Song mSong;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

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

        mSong = (Song) getIntent().getSerializableExtra(SONG_EXTRA);
        loadSong();

        mRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child("DEBUG")
                .child(Database.SONGS)
                .child(mSong.getKey());
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

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                finish();
            }
        });
    }

    private void initViews() {
        /*titleVS = (ViewSwitcher) findViewById(R.id.title_switcher);
        composerVS = (ViewSwitcher) findViewById(R.id.composer_switcher);
        opusVS = (ViewSwitcher) findViewById(R.id.opus_switcher);
        descriptionVS = (ViewSwitcher) findViewById(R.id.description_switcher);
        titleTV = (TextView) findViewById(R.id.title_textView);
        composerTV = (TextView) findViewById(R.id.composer_textView);
        opusTV = (TextView) findViewById(R.id.opus_textView);
        descriptionTV = (TextView) findViewById(R.id.description_textView);*/
        titleET = (EditText) findViewById(R.id.title_editText);
        composerET = (EditText) findViewById(R.id.composer_editText);
        opusET = (EditText) findViewById(R.id.opus_editText);
        descriptionET = (EditText) findViewById(R.id.description_editText);
    }

    private void save() {
        String title = titleET.getText().toString().trim();
        String composer = composerET.getText().toString().trim();
        String opus = opusET.getText().toString().trim();
        String description = descriptionET.getText().toString().trim();
        mSong.setTitle(title.isEmpty() ? null : title);
        mSong.setComposer(composer.isEmpty() ? null : composer);
        mSong.setOpus(opus.isEmpty() ? null : opus);
        mSong.setDescription(description.isEmpty() ? null : description);

        mRef.setValue(mSong);
    }

    private void loadSong() {
        titleET.setText(mSong.getTitle());
        composerET.setText(mSong.getComposer());
        opusET.setText(mSong.getOpus());
        descriptionET.setText(mSong.getDescription());
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
        save();
        super.onDestroy();
    }
}
