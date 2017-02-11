package com.teinvdlugt.android.piano;

public class Song {

    private String title;
    private String composer;
    private String opus;
    private String description;

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
}
