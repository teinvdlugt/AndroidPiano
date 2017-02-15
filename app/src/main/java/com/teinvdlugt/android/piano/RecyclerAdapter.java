package com.teinvdlugt.android.piano;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SONG = 0;
    private static final int VIEW_TYPE_COMPOSER = 1;

    private Context context;
    private List<? extends Listable> data;

    RecyclerAdapter(Context context) {
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

class SongViewHolder extends RecyclerView.ViewHolder {

    private TextView mTitleTV, mComposerTV;
    private Context mContext;
    private Song mSong;

    SongViewHolder(View itemView) {
        super(itemView);
        mTitleTV = (TextView) itemView.findViewById(R.id.title_textView);
        Log.d("spaghetti", "" + itemView);
        mComposerTV = (TextView) itemView.findViewById(R.id.composer_textView);
        mContext = itemView.getContext();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongActivity.openActivity(mContext, mSong);
            }
        });
    }

    void bind(Context context, Song data) {
        mSong = data;
        mTitleTV.setText(data.getTitle());

        // Set composer name & opus text in same TextView
        String composer = data.getComposer(), opus = data.getOpus();
        if (composer == null && opus == null) mComposerTV.setVisibility(View.GONE);
        else mComposerTV.setVisibility(View.VISIBLE);
        if (composer == null && opus != null) mComposerTV.setText(opus);
        else if (composer != null && opus == null) mComposerTV.setText(composer);
        else mComposerTV.setText(context.getString(R.string.composer_opus_format, composer, opus));
    }
}

class ComposerViewHolder extends RecyclerView.ViewHolder {

    private TextView nameTV, songsTV;

    ComposerViewHolder(View itemView) {
        super(itemView);
        nameTV = (TextView) itemView.findViewById(R.id.composer_textView);
        songsTV = (TextView) itemView.findViewById(R.id.songs_textView);
    }

    void bind(Composer composer) {
        nameTV.setText(composer.getName());

        // Create text for songsTV
        StringBuilder sb = new StringBuilder();
        for (Song song : composer.getSongs()) {
            sb.append(song.getTitle()).append("\n");
            // TODO: 15-2-17 Sort by title
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        songsTV.setText(sb);
    }
}
