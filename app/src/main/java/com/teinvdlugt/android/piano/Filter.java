package com.teinvdlugt.android.piano;

import java.io.Serializable;
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
        if (wishList || starred || (searchQuery != null && !searchQuery.isEmpty())) { // Just to prevent useless work
            for (Iterator<Song> iter = result.iterator(); iter.hasNext(); ) {
                Song next = iter.next();
                if ((
                        wishList && !next.isWishList()) ||
                        (starred && !next.isStarred()) ||
                        (searchQuery != null && !next.concatText().contains(searchQuery)))
                    iter.remove();
            }
        }
        return result;
    }

    private boolean wishList = false;
    private boolean starred = false;
    private String searchQuery;

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
