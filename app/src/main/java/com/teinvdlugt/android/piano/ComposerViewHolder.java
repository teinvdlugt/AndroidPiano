package com.teinvdlugt.android.piano;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ComposerViewHolder extends RecyclerView.ViewHolder {

    private TextView nameTV, songsTV;

    public ComposerViewHolder(View itemView) {
        super(itemView);
        nameTV = (TextView) itemView.findViewById(R.id.composer_textView);
        songsTV = (TextView) itemView.findViewById(R.id.songs_textView);
    }

    void bind(String composerName, DatabaseReference songsRef) {
        nameTV.setText(composerName);

        Query query = songsRef.orderByChild(Database.COMPOSER).equalTo(composerName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder sb = new StringBuilder();
                for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                    sb.append(snapShot.child(Database.TITLE).getValue()).append("\n");
                }
                sb.replace(sb.length() - 1, sb.length(), "");
                songsTV.setText(sb);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                songsTV.setText("Error");
            }
        }); // TODO listen for changes
    }
}
