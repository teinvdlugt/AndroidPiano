package com.teinvdlugt.android.piano;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

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
