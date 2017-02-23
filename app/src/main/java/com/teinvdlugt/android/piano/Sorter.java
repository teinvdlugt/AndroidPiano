package com.teinvdlugt.android.piano;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sorter implements Serializable {
    public List<Song> sort(List<Song> songs) {
        List<Song> starred = new ArrayList<>();
        List<Song> notStarred = new ArrayList<>();
        for (Song song : songs) {
            if (song.isStarred())
                starred.add(song);
            else notStarred.add(song);
        }
        List<Song> result = new ArrayList<>();
        result.addAll(starred);
        result.addAll(notStarred);
        return result;
    }
}
