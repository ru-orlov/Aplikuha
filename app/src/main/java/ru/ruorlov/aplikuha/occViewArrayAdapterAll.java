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
 * Created by User on 29.11.2015.
 */
public class occViewArrayAdapterAll extends SimpleCursorAdapter {
    private final Activity context;
    private final Cursor names;

    public occViewArrayAdapterAll(Activity context, Cursor c, String[] from, int[] to, int flags) {
        super(context, R.layout.occasion_item, c, from, to, flags);
        this.context = context;
        this.names = c;
    }

    static class ViewHolder {
        public TextView txtOccName;
        public TextView txtOccStart;
        public TextView txtOccStop;
        public ImageView imgOcc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.occasion_item, null, true);
            holder = new ViewHolder();
            holder.txtOccName = (TextView) rowView.findViewById(R.id.occName);
            holder.txtOccStart = (TextView) rowView.findViewById(R.id.occStart);
            holder.txtOccStop = (TextView) rowView.findViewById(R.id.occEnd);
            holder.imgOcc = (ImageView) rowView.findViewById(R.id.occFile);
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
        Date d_stop = stop.getTime();

        holder.txtOccName.setText(names.getString(names.getColumnIndex("name")));
        holder.txtOccStart.setText(sdf.format(d_start));
        holder.txtOccStop.setText(sdf.format(d_stop));
        ImageManager.fetchImage(names.getString(names.getColumnIndex("file")), holder.imgOcc);


        return rowView;
    }
}
