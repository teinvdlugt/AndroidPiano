package com.teinvdlugt.android.piano.practise;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.piano.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private Context context;
    private List<Session> data;
    private DateFormat dateFormat;

    SessionAdapter(Context context, List<Session> data) {
        this.context = context;
        this.data = data;
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
    }

    public void setData(List<Session> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.list_item_two_line, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    private void showDialog(Session session) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.session_format,
                        format.format(new Date(session.getTimestamp())),
                        session.getDuration() / 60000, session.getDescription()))
                .setPositiveButton(R.string.ok, null)
                .create().show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1, textView2;
        Session currentData;

        ViewHolder(View itemView) {
            super(itemView);
            textView1 = (TextView) itemView.findViewById(R.id.textView1);
            textView2 = (TextView) itemView.findViewById(R.id.textView2);
            itemView.findViewById(R.id.root)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentData != null) showDialog(currentData);
                        }
                    });
        }

        void bind(Session data) {
            currentData = data;
            textView1.setText(context.getString(R.string.practiseDurationMinutes_format,
                    data.getDuration() / 60000));
            textView2.setText(dateFormat.format(new Date(data.getTimestamp())));
        }
    }
}
