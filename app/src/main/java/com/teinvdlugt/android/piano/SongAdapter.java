package com.teinvdlugt.android.piano;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private Context context;
    private List<Song> data;

    public SongAdapter(Context context) {
        this.context = context;
    }

    public void setData(java.util.List<Song> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_song, parent, false));
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        holder.bind(context, data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }
}
