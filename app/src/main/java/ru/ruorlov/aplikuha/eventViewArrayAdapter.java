package ru.ruorlov.aplikuha;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 19.12.2015.
 */
public class eventViewArrayAdapter extends SimpleCursorAdapter {

    private final Activity context;
    private final Cursor names;

    public eventViewArrayAdapter(Activity context, Cursor c, String[] from, int[] to, int flags) {
        super(context, R.layout.events_items, c, from, to, flags);
        this.context = context;
        this.names = c;
    }

    static class ViewHolder {
        public TextView txtEventName;
        public TextView txtEventStart;
        public TextView txtEventStop;
        public ImageView imgEvent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.events_items, null, true);
            holder = new ViewHolder();
            holder.txtEventName = (TextView) rowView.findViewById(R.id.event_name);
            holder.txtEventStart = (TextView) rowView.findViewById(R.id.event_start);
            holder.txtEventStop = (TextView) rowView.findViewById(R.id.event_stop);
            holder.imgEvent = (ImageView) rowView.findViewById(R.id.event_file);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        names.moveToPosition(position);

        Calendar start = Calendar.getInstance();
        Calendar stop = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        start.setTimeInMillis(Long.parseLong(names.getString(names.getColumnIndex("start"))) * 1000);
        stop.setTimeInMillis(Long.parseLong(names.getString(names.getColumnIndex("end"))) * 1000);
        Date d_start = start.getTime();
        Date d_stop = start.getTime();

        holder.txtEventName.setText(names.getString(names.getColumnIndex("name")));
        holder.txtEventStart.setText(sdf.format(d_start));
        holder.txtEventStop.setText(sdf.format(d_stop));
        ImageManager.fetchImage(names.getString(names.getColumnIndex("file")), holder.imgEvent);

        return rowView;
    }
}


