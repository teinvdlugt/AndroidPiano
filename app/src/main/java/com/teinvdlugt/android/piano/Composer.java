package com.teinvdlugt.android.piano;

import java.util.ArrayList;
import java.util.List;

public class Composer implements Listable {

    /**
     * From the list of songs, create a list of Composers,
     * each with a name and songs assigned to them
     */
    public static List<Composer> getComposers(List<Song> songs) {
        List<Composer> result = new ArrayList<>();
        for (Song song : songs) {
            int index = getComposerIndex(result, song.getComposer());
            if (index == -1) {
                Composer newComposer = new Composer();
                newComposer.setName(song.getComposer());
                newComposer.songs.add(song);
                result.add(newComposer);
            } else {
                result.get(index).songs.add(song);
            }
        }
        return result;
    }

    /**
     * Returns the index in the List of the Composer with composerName as name,
     * or -1 if not present.
     */
    public static int getComposerIndex(List<Composer> list, String composerName) {
        for (int i = 0; i < list.size(); i++) {
            if (composerName.equals(list.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private String name;
    private List<Song> songs = new ArrayList<>();

    public Composer() {}

    public Composer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        this.songs.add(song);
    }
}
