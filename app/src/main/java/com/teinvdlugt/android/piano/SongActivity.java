package com.teinvdlugt.android.piano;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
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
    private RadioGroup stateRG;
    private SwitchCompat wishListSW, byHeartSW;

    private boolean removed;

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
                if (mSong != null) loadSong();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SongActivity.this, "Something failed", Toast.LENGTH_SHORT).show();
                // TODO: 11-2-17 Remove before publishing
            }
        };
        mRef.addValueEventListener(mValueListener);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                finish();
            }
        });
        findViewById(R.id.remove_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SongActivity.this)
                        .setMessage(R.string.remove_dialog_message)
                        .setPositiveButton(R.string.button_remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRef.removeValue();
                                removed = true;
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create().show();
            }
        });

        findViewById(R.id.byHeart_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byHeartSW.setChecked(!byHeartSW.isChecked());
            }
        });
        findViewById(R.id.wishList_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wishListSW.setChecked(!wishListSW.isChecked());
            }
        });
    }

    private void initViews() {
        titleET = (EditText) findViewById(R.id.title_editText);
        composerET = (EditText) findViewById(R.id.composer_editText);
        opusET = (EditText) findViewById(R.id.opus_editText);
        descriptionET = (EditText) findViewById(R.id.description_editText);
        stateRG = (RadioGroup) findViewById(R.id.state_radioGroup);
        wishListSW = (SwitchCompat) findViewById(R.id.wishList_switch);
        byHeartSW = (SwitchCompat) findViewById(R.id.byHeart_switch);
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
        mSong.setWishList(wishListSW.isChecked());
        mSong.setByHeart(byHeartSW.isChecked());

        // Save stateRG state
        switch (stateRG.getCheckedRadioButtonId()) {
            case R.id.stateNotLearning_radioButton:
                mSong.setState(Database.STATE_NOT_LEARNING);
                break;
            case R.id.stateCurrentlyLearning_radioButton:
                mSong.setState(Database.STATE_CURRENTLY_LEARNING);
                break;
            case R.id.stateDoneLearning_radioButton:
                mSong.setState(Database.STATE_DONE_LEARNING);
                break;
        }

        mRef.setValue(mSong);
    }

    private void loadSong() {
        getSupportActionBar().setTitle(mSong.getTitle());
        titleET.setText(mSong.getTitle());
        composerET.setText(mSong.getComposer());
        opusET.setText(mSong.getOpus());
        descriptionET.setText(mSong.getDescription());
        wishListSW.setChecked(mSong.isWishList());
        byHeartSW.setChecked(mSong.isByHeart());

        // Set "State" RadioGroup selection
        switch (mSong.getState()) {
            case Database.STATE_NOT_LEARNING:
                stateRG.check(R.id.stateNotLearning_radioButton);
                break;
            case Database.STATE_CURRENTLY_LEARNING:
                stateRG.check(R.id.stateCurrentlyLearning_radioButton);
                break;
            case Database.STATE_DONE_LEARNING:
                stateRG.check(R.id.stateDoneLearning_radioButton);
                break;
        }
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
        if (!removed) save();
        super.onDestroy();
    }

    public static void openActivity(Context context, Song song) {
        context.startActivity(new Intent(context, SongActivity.class)
                .putExtra(SongActivity.SONG_EXTRA, song));
    }
}
