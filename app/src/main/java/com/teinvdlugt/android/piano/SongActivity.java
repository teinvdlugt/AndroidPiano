package com.teinvdlugt.android.piano;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SongActivity extends AppCompatActivity {
    public static final String SONG_EXTRA = "song";
    public static final String COMPOSER_NAMES_EXTRA = "composerNames";
    public static final String TAGS_EXTRA = "tags";

    private Song mSong;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

    private EditText titleET, opusET, descriptionET;
    private AutoCompleteTextView composerACTV;
    private MultiAutoCompleteTextView tagsMACTV;
    private TagLayout tagLayout;
    private RadioGroup stateRG;
    private SwitchCompat wishListSW, byHeartSW;

    private boolean removed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not signed in; return to MainActivity to handle with sign in
            finish();
            return;
        }

        initViews();
        setupAutoComplete();

        mSong = (Song) getIntent().getSerializableExtra(SONG_EXTRA);
        loadSong();

        mRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(user.getUid())
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

    private void setupAutoComplete() {
        // Composer auto complete
        ArrayList<String> composerNames = (ArrayList<String>) getIntent().getSerializableExtra(COMPOSER_NAMES_EXTRA);
        if (composerNames != null && !composerNames.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, composerNames);
            composerACTV.setAdapter(adapter);
            composerACTV.setThreshold(0);
        }

        // Tags auto complete
        ArrayList<String> tags = (ArrayList<String>) getIntent().getSerializableExtra(TAGS_EXTRA);
        if (tags != null && !tags.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, tags);
            tagsMACTV.setAdapter(adapter);
            tagsMACTV.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            tagsMACTV.setThreshold(0);
        }
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
        findViewById(R.id.startedLearningDate_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long date = mSong.getStartedLearningDate();
                createDatePicker(date, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Date date = createDate(year, month, dayOfMonth);
                        mSong.setStartedLearningDate(date.getTime());
                        setDateText(R.id.startedLearningDate_textView, R.string.startedLearning_format,
                                DateFormat.getDateInstance().format(date));
                    }
                }).show();
            }
        });

        findViewById(R.id.startedLearningDate_clear_imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSong.setStartedLearningDate(null);
                setDateText(R.id.startedLearningDate_textView, R.string.startedLearning_format,
                        getString(R.string.date_not_set));
            }
        });

        tagLayout.setOnTagClickListener(new TagLayout.OnTagClickListener() {
            @Override
            public void onClickTag(String tag) {
                Toast.makeText(SongActivity.this, "Lol. You clicked " + tag + "!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private DatePickerDialog createDatePicker(Long timeInMillis, DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar calendar = Calendar.getInstance();
        if (timeInMillis != null)
            calendar.setTimeInMillis(timeInMillis);
        return new DatePickerDialog(this, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static Date createDate(int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return cal.getTime();
    }

    private void setDateText(@IdRes int textViewId, @StringRes int format, String dateString) {
        String fullString = getString(format, dateString);
        SpannableString ss = new SpannableString(fullString);
        ForegroundColorSpan grey = new ForegroundColorSpan(getColor(this, R.color.textColorSecondary));
        ss.setSpan(grey, 0, fullString.length() - dateString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(textViewId)).setText(ss);
    }

    private void initViews() {
        titleET = (EditText) findViewById(R.id.title_editText);
        composerACTV = (AutoCompleteTextView) findViewById(R.id.composer_autoCompleteTextView);
        opusET = (EditText) findViewById(R.id.opus_editText);
        descriptionET = (EditText) findViewById(R.id.description_editText);
        stateRG = (RadioGroup) findViewById(R.id.state_radioGroup);
        wishListSW = (SwitchCompat) findViewById(R.id.wishList_switch);
        byHeartSW = (SwitchCompat) findViewById(R.id.byHeart_switch);
        tagsMACTV = (MultiAutoCompleteTextView) findViewById(R.id.tags_multiAutoCompleteTextView);
        tagLayout = (TagLayout) findViewById(R.id.tagLayout);
    }

    private void save() {
        if (mSong == null) mSong = new Song();
        String title = titleET.getText().toString().trim();
        String composer = composerACTV.getText().toString().trim();
        String opus = opusET.getText().toString().trim();
        String description = descriptionET.getText().toString().trim();
        String tags = tagsMACTV.getText().toString().trim();
        mSong.setTitle(title.isEmpty() ? null : title);
        mSong.setComposer(composer.isEmpty() ? null : composer);
        mSong.setOpus(opus.isEmpty() ? null : opus);
        mSong.setDescription(description.isEmpty() ? null : description);
        mSong.setTags(tags.isEmpty() ? null : tags);
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
        composerACTV.setText(mSong.getComposer());
        opusET.setText(mSong.getOpus());
        descriptionET.setText(mSong.getDescription());
        tagsMACTV.setText(mSong.getTags());
        wishListSW.setChecked(mSong.isWishList());
        byHeartSW.setChecked(mSong.isByHeart());
        setDateText(R.id.startedLearningDate_textView, R.string.startedLearning_format,
                mSong.getStartedLearningDate() == null ? getString(R.string.date_not_set)
                        : DateFormat.getDateInstance().format(mSong.getStartedLearningDate()));

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

        if (mSong.getTags() != null) tagLayout.setTags(mSong.getTags());

        // The state of the star in the menu bar is determined in
        // onCreateOptionsMenu. That gets called after mSong is retrieved,
        // so calling invalidateOptionsMenu() here isn't necessary.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_song, menu);
        if (mSong != null) {
            MenuItem star = menu.findItem(R.id.star_menuAction);
            star.setIcon(mSong.isStarred() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.star_menuAction:
                mSong.setStarred(!mSong.isStarred());
                invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mRef.removeEventListener(mValueListener);
        if (!removed) save();
        super.onDestroy();
    }

    public static int getColor(Context context, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23)
            return context.getColor(colorId);
        else return context.getResources().getColor(colorId);
    }

    public static void openActivity(Context context, Song song,
                                    ArrayList<String> composerNames, ArrayList<String> tags) {
        context.startActivity(new Intent(context, SongActivity.class)
                .putExtra(SongActivity.SONG_EXTRA, song)
                .putExtra(COMPOSER_NAMES_EXTRA, composerNames)
                .putExtra(TAGS_EXTRA, tags));
    }
}
