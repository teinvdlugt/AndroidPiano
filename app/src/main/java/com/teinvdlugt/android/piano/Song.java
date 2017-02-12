package com.teinvdlugt.android.piano;

import java.io.Serializable;

class Song implements Serializable {

    private String title;
    private String composer;
    private String opus;
    private String description;
    private boolean currentlyLearning, done, byHeart;

    private String key;

    Song() {}

    Song(String title, String composer, String opus, String description) {
        this.title = title;
        this.composer = composer;
        this.opus = opus;
        this.description = description;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getComposer() {
        return composer;
    }

    void setComposer(String composer) {
        this.composer = composer;
    }

    String getOpus() {
        return opus;
    }

    void setOpus(String opus) {
        this.opus = opus;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    boolean isCurrentlyLearning() {
        return currentlyLearning;
    }

    void setCurrentlyLearning(boolean currentlyLearning) {
        this.currentlyLearning = currentlyLearning;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }

    boolean isByHeart() {
        return byHeart;
    }

    void setByHeart(boolean byHeart) {
        this.byHeart = byHeart;
    }
}
