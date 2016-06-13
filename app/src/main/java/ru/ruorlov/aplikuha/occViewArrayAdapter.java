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
 * Created by User on 25.11.2015.
 */
public class occViewArrayAdapter extends SimpleCursorAdapter {
    private final Activity context;
    private final Cursor names;

    public occViewArrayAdapter(Activity context, Cursor c, String[] from, int[] to, int flags) {
        super(context, R.layout.places_items, c, from, to, flags);
        this.context = context;
        this.names = c;
    }

    static class ViewHolder {
        public TextView txtPlaceName;
        public TextView txtPlaceStart;
        public TextView txtPlaceStop;
        public ImageView imgPlace;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.places_items, null, true);
            holder = new ViewHolder();
            holder.txtPlaceName = (TextView) rowView.findViewById(R.id.place_name);
            holder.txtPlaceStart = (TextView) rowView.findViewById(R.id.place_start);
            holder.txtPlaceStop = (TextView) rowView.findViewById(R.id.place_stop);
            holder.imgPlace = (ImageView) rowView.findViewById(R.id.place_file);
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

        holder.txtPlaceName.setText(names.getString(names.getColumnIndex("name")));
        holder.txtPlaceStart.setText(sdf.format(d_start));
        holder.txtPlaceStop.setText(sdf.format(d_stop));
        ImageManager.fetchImage(names.getString(names.getColumnIndex("file")), holder.imgPlace);

        return rowView;
    }

}

