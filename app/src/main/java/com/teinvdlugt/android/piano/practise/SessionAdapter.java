package com.teinvdlugt.android.piano.practise;

import android.content.Context;
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
        dateFormat = DateFormat.getDateTimeInstance();
    }

    public void setData(List<Session> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(android.R.layout.two_line_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1, textView2;

        ViewHolder(View itemView) {
            super(itemView);
            textView1 = (TextView) itemView.findViewById(android.R.id.text1);
            textView2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

        void bind(Session data) {
            textView1.setText(context.getString(R.string.practiseDurationMinutes_format,
                    data.getDuration() / 60000));
            textView2.setText(dateFormat.format(new Date(data.getTimestamp())));
        }
    }
}
