package com.teinvdlugt.android.piano.practise;

public class Session {
    private long timestamp; // In milliseconds
    private long duration;  // In milliseconds

    public Session(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
