package com.teinvdlugt.android.piano;

import java.io.Serializable;

public class Song implements Serializable {

    private String title;
    private String composer;
    private String opus;
    private String description;

    private String key;

    public Song() {}

    public Song(String title, String composer, String opus, String description) {
        this.title = title;
        this.composer = composer;
        this.opus = opus;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getOpus() {
        return opus;
    }

    public void setOpus(String opus) {
        this.opus = opus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
