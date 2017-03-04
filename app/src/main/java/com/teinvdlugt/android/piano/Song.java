package com.teinvdlugt.android.piano;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Song implements Serializable, Listable {

    static List<Song> getSongsList(DataSnapshot snapshot) {
        List<Song> result = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Song song = child.getValue(Song.class);
            song.setKey(child.getKey());
            result.add(song);
        }
        return result;
    }

    static ArrayList<String> getTags(List<Song> songs) {
        Set<String> set = new HashSet<>();
        for (Song song : songs) {
            List<String> tags = song.createTagsList();
            set.addAll(tags);
        }
        return new ArrayList<>(set);
    }

    private String title;
    private String composer;
    private String opus;
    private String description;
    private String state = Database.STATE_NOT_LEARNING;
    private boolean wishList;
    private boolean byHeart;
    private boolean starred;
    private Long startedLearningDate; // In milliseconds
    private String tags;

    private String key;

    /**
     * @return Concatenation of all text fields of this song. Handy for
     * search operation in Filter.java.
     */
    String concatText() {
        StringBuilder sb = new StringBuilder();
        for (String s : new String[]{title, composer, opus, description}) {
            if (!(s == null || s.isEmpty())) sb.append(" ").append(s);
        }
        if (tags != null) {
            for (String tag : tags.split(","))
                sb.append(" ").append(tag);
        }
        return sb.toString().trim();
    }

    private ArrayList<String> createTagsList() {
        if (tags == null) return new ArrayList<>();
        String[] array = tags.split(",");
        ArrayList<String> list = new ArrayList<>();
        for (String tag : array)
            list.add(tag.trim());
        return list;
    }

    Song() {}

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

    boolean isByHeart() {
        return byHeart;
    }

    void setByHeart(boolean byHeart) {
        this.byHeart = byHeart;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isWishList() {
        return wishList;
    }

    public void setWishList(boolean wishList) {
        this.wishList = wishList;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public Long getStartedLearningDate() {
        return startedLearningDate;
    }

    public void setStartedLearningDate(Long startedLearningDate) {
        this.startedLearningDate = startedLearningDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
