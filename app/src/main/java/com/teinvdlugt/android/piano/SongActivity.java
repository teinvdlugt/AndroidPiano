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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class SongActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String SONG_EXTRA = "song";
    public static final String SONG_KEY_EXTRA = "songKey";
    public static final String COMPOSER_NAMES_EXTRA = "composerNames";
    public static final String TAGS_EXTRA = "tags";

    private Song mSong;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;

    private RadioGroup stateRG;
    private SwitchCompat wishListSW, byHeartSW;

    // Views in edit mode
    private ViewGroup editingLayout;
    private EditText titleET, opusET, descriptionET;
    private AutoCompleteTextView composerACTV;
    private MultiAutoCompleteTextView tagsMACTV;

    // View in no-edit mode
    private ViewGroup notEditingLayout;
    private TextView titleTV, opusTV, descriptionTV, composerTV;
    private TagLayout tagLayout;

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

        String songKey = getIntent().getStringExtra(SONG_KEY_EXTRA);
        mRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(user.getUid())
                .child(Database.SONGS)
                .child(songKey);
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSong = dataSnapshot.getValue(Song.class);
                if (mSong == null) {
                    // Song was probably removed
                    finish();
                } else {
                    loadSong();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SongActivity.this, "Something failed", Toast.LENGTH_SHORT).show();
                // TODO: 11-2-17 Remove before publishing
            }
        };
        mRef.addValueEventListener(mValueListener);

        setOnClickListeners();
        setChangeListeners();
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
        findViewById(R.id.byHeart_layout).setOnClickListener(this);
        findViewById(R.id.wishList_layout).setOnClickListener(this);
        findViewById(R.id.startedLearningDate_layout).setOnClickListener(this);
        findViewById(R.id.startedLearningDate_clear_imageButton).setOnClickListener(this);
        findViewById(R.id.edit_imageButton).setOnClickListener(this);
        findViewById(R.id.save_imageButton).setOnClickListener(this);

        tagLayout.setOnTagClickListener(new TagLayout.OnTagClickListener() {
            @Override
            public void onClickTag(String tag) {
                Toast.makeText(SongActivity.this, "Lol. You clicked " + tag + "!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.byHeart_layout:
                byHeartSW.setChecked(!byHeartSW.isChecked());
                break;
            case R.id.wishList_layout:
                wishListSW.setChecked(!wishListSW.isChecked());
                break;
            case R.id.startedLearningDate_layout:
                Long date = mSong.getStartedLearningDate();
                createDatePicker(date, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Date date = createDate(year, month, dayOfMonth);
                        mSong.setStartedLearningDate(date.getTime());
                        mRef.setValue(mSong);
                        // Firebase listener handles the UI change
                    }
                }).show();
                break;
            case R.id.startedLearningDate_clear_imageButton:
                mSong.setStartedLearningDate(null);
                mRef.setValue(mSong);
                // Firebase listener handles the UI change
                break;
            case R.id.edit_imageButton:
                editingLayout.setVisibility(View.VISIBLE);
                notEditingLayout.setVisibility(View.GONE);
                break;
            case R.id.save_imageButton:
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
                mRef.setValue(mSong);

                editingLayout.setVisibility(View.GONE);
                notEditingLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setChangeListeners() {
        stateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
                    case R.id.statePerformanceReady_radioButton:
                        mSong.setState(Database.STATE_PERFORMANCE_READY);
                        break;
                }
                mRef.setValue(mSong);
            }
        });

        wishListSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSong.setWishList(isChecked);
                mRef.setValue(mSong);
            }
        });
        byHeartSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSong.setByHeart(isChecked);
                mRef.setValue(mSong);
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
        stateRG = (RadioGroup) findViewById(R.id.state_radioGroup);
        wishListSW = (SwitchCompat) findViewById(R.id.wishList_switch);
        byHeartSW = (SwitchCompat) findViewById(R.id.byHeart_switch);

        // Views in edit mode
        editingLayout = (ViewGroup) findViewById(R.id.texts_edit_layout);
        titleET = (EditText) findViewById(R.id.title_editText);
        composerACTV = (AutoCompleteTextView) findViewById(R.id.composer_autoCompleteTextView);
        opusET = (EditText) findViewById(R.id.opus_editText);
        descriptionET = (EditText) findViewById(R.id.description_editText);
        tagsMACTV = (MultiAutoCompleteTextView) findViewById(R.id.tags_multiAutoCompleteTextView);

        // Views in no-edit mode
        notEditingLayout = (ViewGroup) findViewById(R.id.texts_noEdit_layout);
        tagLayout = (TagLayout) findViewById(R.id.tagLayout);
        titleTV = (TextView) findViewById(R.id.title_textView);
        opusTV = (TextView) findViewById(R.id.opus_textView);
        descriptionTV = (TextView) findViewById(R.id.description_textView);
        composerTV = (TextView) findViewById(R.id.composer_textView);
    }

    private void loadSong() {
        getSupportActionBar().setTitle(mSong.getTitle());
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
            case Database.STATE_PERFORMANCE_READY:
                stateRG.check(R.id.statePerformanceReady_radioButton);
                break;
        }
        wishListSW.setChecked(mSong.isWishList());
        byHeartSW.setChecked(mSong.isByHeart());
        setDateText(R.id.startedLearningDate_textView, R.string.startedLearning_format,
                mSong.getStartedLearningDate() == null ? getString(R.string.date_not_set)
                        : DateFormat.getDateInstance().format(mSong.getStartedLearningDate()));

        // Views in edit mode
        titleET.setText(mSong.getTitle());
        composerACTV.setText(mSong.getComposer());
        opusET.setText(mSong.getOpus());
        descriptionET.setText(mSong.getDescription());
        tagsMACTV.setText(mSong.getTags());

        // Views in no-edit mode
        titleTV.setText(mSong.getTitle() == null ? getString(R.string.untitled) : mSong.getTitle());
        opusTV.setText(mSong.getOpus());
        opusTV.setVisibility(mSong.getOpus() == null ? View.GONE : View.VISIBLE);
        descriptionTV.setText(mSong.getDescription());
        descriptionTV.setVisibility(mSong.getDescription() == null ? View.GONE : View.VISIBLE);
        composerTV.setText(mSong.getComposer());
        composerTV.setVisibility(mSong.getComposer() == null ? View.GONE : View.VISIBLE);
        if (mSong.getTags() != null) tagLayout.setTags(mSong.getTags());
        tagLayout.setVisibility(mSong.getTags() == null ? View.GONE : View.VISIBLE);

        // If the title is not set, assume that it is a new entry and
        // switch to editing mode and focus titleET
        if (mSong.getTitle() == null) {
            editingLayout.setVisibility(View.VISIBLE);
            notEditingLayout.setVisibility(View.GONE);
            titleET.requestFocus();
        }

        invalidateOptionsMenu();
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
                mRef.setValue(mSong);
                return true;
            case R.id.remove_menuAction:
                new AlertDialog.Builder(SongActivity.this)
                        .setMessage(R.string.remove_dialog_message)
                        .setPositiveButton(R.string.button_remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRef.removeValue();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mRef.removeEventListener(mValueListener);
        super.onDestroy();
    }

    public static int getColor(Context context, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= 23)
            return context.getColor(colorId);
        else return context.getResources().getColor(colorId);
    }

    public static void openActivity(Context context, String key,
                                    ArrayList<String> composerNames, ArrayList<String> tags) {
        context.startActivity(new Intent(context, SongActivity.class)
                .putExtra(SongActivity.SONG_KEY_EXTRA, key)
                .putExtra(COMPOSER_NAMES_EXTRA, composerNames)
                .putExtra(TAGS_EXTRA, tags));
    }
}
