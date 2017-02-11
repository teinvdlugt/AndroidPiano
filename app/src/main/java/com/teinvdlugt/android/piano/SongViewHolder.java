package com.teinvdlugt.android.piano;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class SongViewHolder extends RecyclerView.ViewHolder {

    private TextView mTitleTV, mComposerTV;
    private Context mContext;

    public SongViewHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, SongActivity.class)
                        .putExtra(SongActivity.SONG_ID, "1")); // TODO: 11-2-17 Retrieve Song key
            }
        });
        mTitleTV = (TextView) itemView.findViewById(R.id.title_textView);
        mComposerTV = (TextView) itemView.findViewById(R.id.composer_textView);
    }

    void bind(Context context, Song data) {
        mTitleTV.setText(data.getTitle());
        mComposerTV.setText(context.getString(R.string.composer_opus_format,
                data.getComposer(), data.getOpus()));
    }
}
