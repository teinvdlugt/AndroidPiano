package com.teinvdlugt.android.piano;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

class SongViewHolder extends RecyclerView.ViewHolder {

    private TextView mTitleTV, mComposerTV;
    private Context mContext;
    private Song mSong;

    public SongViewHolder(View itemView) {
        super(itemView);
        mTitleTV = (TextView) itemView.findViewById(R.id.title_textView);
        Log.d("spaghetti", "" + itemView);
        mComposerTV = (TextView) itemView.findViewById(R.id.composer_textView);
        mContext = itemView.getContext();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, SongActivity.class)
                        .putExtra(SongActivity.SONG_EXTRA, mSong));
            }
        });
    }

    void bind(Context context, Song data, String key) {
        mSong = data;
        data.setKey(key);
        mTitleTV.setText(data.getTitle());
        mComposerTV.setText(context.getString(R.string.composer_opus_format,
                data.getComposer(), data.getOpus()));
    }
}
