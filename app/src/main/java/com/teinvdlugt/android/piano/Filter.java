package com.teinvdlugt.android.piano;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Filter implements Serializable {
    /**
     * Creates a new ArrayList object containing just the songs according to the filter settings.
     *
     * @param list The List to filter. Won't be modified.
     */
    public List<Song> filter(List<Song> list) {
        List<Song> result = new ArrayList<>();
        result.addAll(list);
        if (wishList || starred || (searchQuery != null && !searchQuery.isEmpty())
                || (tags != null && !tags.trim().isEmpty())) { // Just to prevent useless work
            for (Iterator<Song> iter = result.iterator(); iter.hasNext(); ) {
                Song next = iter.next();

                // Remove songs according to filter
                if ((
                        wishList && !next.isWishList()) ||
                        (starred && !next.isStarred()) ||
                        (!matchesSearchQuery(next))) {
                    iter.remove();
                    continue;
                }

                // Also remove songs with unsufficient tags
                List<String> songTags = Song.splitTags(next.getTags());
                List<String> filterTags = Song.splitTags(tags);
                for (String filterTag : filterTags) {
                    if (!songTags.contains(filterTag)) {
                        iter.remove();
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean matchesSearchQuery(Song song) {
        if (searchQuery == null || searchQuery.isEmpty()) return true;
        String songText = song.concatText();
        return songText != null && normalizeString(songText).contains(normalizeString(searchQuery));
    }

    private static String normalizeString(String string) {
        // Remove accents from characters and convert to lower case.
        string = string.toLowerCase();
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("\\p{M}", "");
        return string;
    }

    public static Filter tagFilter(String tag) {
        Filter filter = new Filter();
        filter.tags = tag;
        return filter;
    }

    private boolean wishList = false;
    private boolean starred = false;
    private String searchQuery;
    private String tags;

    public boolean getWishList() {
        return wishList;
    }

    public void setWishList(boolean wishList) {
        this.wishList = wishList;
    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
