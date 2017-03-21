package com.teinvdlugt.android.piano.practise;

public class Session {
    private long timestamp; // In milliseconds
    private long duration;  // In milliseconds
    private String description;

    Session() {}

    Session(long timestamp, long duration, String description) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.description = description;
    }

    long getTimestamp() {
        return timestamp;
    }

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    long getDuration() {
        return duration;
    }

    void setDuration(long duration) {
        this.duration = duration;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }
}
