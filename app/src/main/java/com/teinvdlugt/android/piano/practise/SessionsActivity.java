package com.teinvdlugt.android.piano.practise;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.teinvdlugt.android.piano.Database;
import com.teinvdlugt.android.piano.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SessionsActivity extends AppCompatActivity {
    public static final String SONG_KEY_EXTRA = "sessions_key";

    private SessionAdapter mAdapter;
    private DatabaseReference mRef;
    private ValueEventListener mValueListener;
    private List<Session> mSessions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not signed in; return to MainActivity to handle with sign in
            finish();
            return;
        }

        String refKey = getIntent().getStringExtra(SONG_KEY_EXTRA);
        if (refKey == null) finish();
        String songKey = getIntent().getStringExtra(SONG_KEY_EXTRA);
        mRef = Database.getDatabaseInstance().getReference()
                .child(Database.USERS)
                .child(user.getUid())
                .child(Database.SONGS)
                .child(songKey)
                .child(Database.SESSIONS);
        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                if (mSessions == null) mSessions = new ArrayList<>();
                else mSessions.clear();
                for (DataSnapshot child : children) {
                    mSessions.add(child.getValue(Session.class));
                }*/
                GenericTypeIndicator<List<Session>> t = new GenericTypeIndicator<List<Session>>() {};
                mSessions = dataSnapshot.getValue(t);
                if (mSessions != null) {
                    sortSessions();
                    mAdapter.setData(mSessions);
                } else mSessions = new ArrayList<>();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SessionsActivity.this, "Something failed", Toast.LENGTH_SHORT).show();
                // TODO: 11-2-17 Remove before publishing
            }
        };
        mRef.addValueEventListener(mValueListener);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.practise_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SessionAdapter(this, null);
        recyclerView.setAdapter(mAdapter);
    }

    public void onClickNewSession(View v) {
        String[] items = {getString(R.string.add_session), getString(R.string.start_session)};
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) onClickAddSession();
                        else onClickStartSession();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void onClickAddSession() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_session)
                .setView(R.layout.dialog_add_session)
                .create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long date = Calendar.getInstance().getTimeInMillis(); // TODO
                        long duration = Long.parseLong(
                                ((EditText) ((AlertDialog) dialog).findViewById(R.id.duration_editText))
                                        .getText().toString()) * 60000;
                        String description =
                                ((EditText) ((AlertDialog) dialog).findViewById(R.id.sessionDescription_editText))
                                        .getText().toString();
                        if (description.isEmpty()) description = null;
                        Session newSession = new Session(date, duration, description);
                        mSessions.add(newSession);
                        mRef.setValue(mSessions);
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                (DialogInterface.OnClickListener) null);
        dialog.show();
        EditText durationET =(EditText) dialog.findViewById(R.id.duration_editText);
        durationET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setEnabled(!(s.length() < 1 || !isValidInteger(s.toString())));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        durationET.setText(null); // Invoke TextWatcher to disable positive button
    }

    private void onClickStartSession() {
        // TODO: 19-3-17 Show dialog; when OK is clicked, start session and show notification with stop button
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortSessions() {
        if (mSessions == null || mSessions.size() <= 1) return;
        for (int i = mSessions.size(); i > 1; i--) {
            for (int j = 0; j < i - 1; j++) {
                if (mSessions.get(j).getTimestamp() < mSessions.get(j + 1).getTimestamp()) {
                    Session atJ = mSessions.get(j);
                    mSessions.set(j, mSessions.get(j + 1));
                    mSessions.set(j + 1, atJ);
                }
            }
        }
    }

    public static void openActivity(Context context, String songKey) {
        context.startActivity(new Intent(context, SessionsActivity.class).putExtra(SONG_KEY_EXTRA, songKey));
    }

    private static boolean isValidInteger(String string) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        mRef.removeEventListener(mValueListener);
        super.onDestroy();
    }
}
