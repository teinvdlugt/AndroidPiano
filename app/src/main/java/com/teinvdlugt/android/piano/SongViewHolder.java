package com.teinvdlugt.android.piano;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class SongViewHolder extends RecyclerView.ViewHolder {

    private TextView mTitleTV, mComposerTV;

    public SongViewHolder(View itemView) {
        super(itemView);
        mTitleTV = (TextView) itemView.findViewById(R.id.title_textView);
        mComposerTV = (TextView) itemView.findViewById(R.id.composer_textView);
    }

    public void bind(Song data) {
        mTitleTV.setText(data.getTitle());
        mComposerTV.setText(data.getComposer());
    }
}
