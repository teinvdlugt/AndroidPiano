package com.teinvdlugt.android.piano;

import com.google.firebase.database.FirebaseDatabase;

public class Database {
    public static final String USERS = "users";
    public static final String SONGS = "songs";
    public static final String PEOPLE = "people";

    // Properties of songs
    public static final String TITLE = "title";
    public static final String COMPOSER = "composer";
    public static final String OPUS = "opus";
    public static final String DESCRIPTION = "description";
    public static final String SESSIONS = "sessions";

    // Song 'state' enums
    static final String STATE_NOT_LEARNING = "not_learning";
    static final String STATE_CURRENTLY_LEARNING = "currently_learning";
    static final String STATE_DONE_LEARNING = "done_learning";
    static final String STATE_PERFORMANCE_READY = "performance_ready";

    // Get FirebaseDatabase instance across Activity lifecycles
    // Otherwise, FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    // crashes the app when the screen is rotated
    private static FirebaseDatabase databaseInstance;

    public static FirebaseDatabase getDatabaseInstance() {
        if (databaseInstance == null) {
            databaseInstance = FirebaseDatabase.getInstance();
            databaseInstance.setPersistenceEnabled(true);
        }
        return databaseInstance;
    }
}
