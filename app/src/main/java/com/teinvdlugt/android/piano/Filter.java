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
        if (wishList) {
            for (Iterator<Song> iter = result.iterator(); iter.hasNext(); ) {
                if (!iter.next().isWishList())
                    iter.remove();
            }
        }
        return result;
    }

    private boolean wishList = false;

    public boolean isWishList() {
        return wishList;
    }

    public void setWishList(boolean wishList) {
        this.wishList = wishList;
    }
}
