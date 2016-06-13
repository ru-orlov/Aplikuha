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
public class commentsViewArrayAdapter extends SimpleCursorAdapter {

    private final Activity context;
    private final Cursor names;



    public commentsViewArrayAdapter(Activity context, Cursor c, String[] from, int[] to, int flags) {
        super(context, R.layout.comment_item, c, from, to, flags);
        this.context = context;
        this.names = c;
    }

    static class ViewHolder {
        public TextView txtComment_id;
        public TextView txtComment_author_name;
        public TextView txtComment_entered;
        public TextView txtComment_message;
        public ImageView imgComment_file;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.comment_item, null, true);
            holder = new ViewHolder();
            holder.txtComment_id = (TextView) rowView.findViewById(R.id.comment_id);
            holder.txtComment_author_name = (TextView) rowView.findViewById(R.id.comment_author_name);
            holder.txtComment_entered = (TextView) rowView.findViewById(R.id.comment_entered);
            holder.txtComment_message = (TextView) rowView.findViewById(R.id.comment_message);
            holder.imgComment_file = (ImageView) rowView.findViewById(R.id.comment_file);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        names.moveToPosition(position);

        Calendar cal_entered = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        cal_entered.setTimeInMillis(Long.parseLong(names.getString(names.getColumnIndex("entered"))));
        Date d_cal = cal_entered.getTime();

        holder.txtComment_id.setText(names.getString(names.getColumnIndex("comment_id")));
        holder.txtComment_entered.setText(sdf.format(d_cal));
        holder.txtComment_message.setText(names.getString(names.getColumnIndex("message")));

        if (names.getString(names.getColumnIndex("author_name")).length()<2) {
            holder.txtComment_author_name.setText("Гость");
        } else if (names.getString(names.getColumnIndex("author_name")).contains("true")) {
            holder.txtComment_author_name.setText("Admin");
        }
        else {
            holder.txtComment_author_name.setText(names.getString(names.getColumnIndex("author_name")));
        }

        if (names.getString(names.getColumnIndex("filename")).length()>2) {

            ImageManager.fetchImage(names.getString(names.getColumnIndex("filename")), holder.imgComment_file);
        }

        return rowView;
    }
}


