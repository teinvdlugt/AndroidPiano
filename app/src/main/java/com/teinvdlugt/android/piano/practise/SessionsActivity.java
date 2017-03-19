package com.teinvdlugt.android.piano.practise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import com.teinvdlugt.android.piano.SongActivity;

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
        setContentView(R.layout.activity_practise);
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
                mSessions = dataSnapshot.getValue(new GenericTypeIndicator<List<Session>>() {});
                if (mSessions == null)
                    finish();
                else {
                    sortSessions();
                    mAdapter.setData(mSessions);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SessionsActivity.this, "Something failed", Toast.LENGTH_SHORT).show();
                // TODO: 11-2-17 Remove before publishing
            }
        };
        mRef.addValueEventListener(mValueListener);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SessionAdapter(this, null);
        recyclerView.setAdapter(mAdapter);
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
            for (int j = 0; j < i; j++) {
                if (mSessions.get(j).getTimestamp() < mSessions.get(j + 1).getTimestamp()) {
                    Session atJ = mSessions.get(j);
                    mSessions.set(j, mSessions.get(j + 1));
                    mSessions.set(j + 1, atJ);
                }
            }
        }
    }

    public void openActivity(Context context, String songKey) {
        context.startActivity(new Intent(context, SessionsActivity.class).putExtra(SONG_KEY_EXTRA, songKey));
    }
}
