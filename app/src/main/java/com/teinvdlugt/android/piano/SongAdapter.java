package com.teinvdlugt.android.piano;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SONG = 0;
    private static final int VIEW_TYPE_COMPOSER = 1;

    private Context context;
    private List<? extends Listable> data;

    SongAdapter(Context context) {
        this.context = context;
    }

    void setData(List<? extends Listable> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SONG:
                return new SongViewHolder(LayoutInflater.from(context)
                        .inflate(R.layout.list_item_song, parent, false));
            case VIEW_TYPE_COMPOSER:
                return new ComposerViewHolder(LayoutInflater.from(context)
                        .inflate(R.layout.list_item_composer, parent, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof Song)
            return VIEW_TYPE_SONG;
        else return VIEW_TYPE_COMPOSER;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongViewHolder)
            ((SongViewHolder) holder).bind(context, (Song) data.get(position));
        else if (holder instanceof ComposerViewHolder)
            ((ComposerViewHolder) holder).bind((Composer) data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }
}
