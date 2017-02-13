package com.teinvdlugt.android.piano;

public class Composer {

    private String name;
    private boolean existing;

    public Composer() {}

    public Composer(String name) {
        this.name = name;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
