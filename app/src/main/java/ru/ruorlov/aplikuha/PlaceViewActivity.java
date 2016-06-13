package ru.ruorlov.aplikuha;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 17.12.2015.
 */

//http://aplikuha.ru/#/places/view/#place_id
public class PlaceViewActivity extends AppCompatActivity {
    DBHelper dbHelper;
    private SimpleCursorAdapter CursorViewEvents;

    private ListView LvEvents;

    Intent intent;
    String occ_id;
    String place_idd;
    String event_idd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_view);
        LvEvents = (ListView) findViewById(R.id.lv_place_view);


        intent = getIntent();
        occ_id = intent.getStringExtra("occ_idd");
        place_idd = intent.getStringExtra("place_idd");
        fillTitleFields();

        LvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((eventViewArrayAdapter) LvEvents.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                event_idd = cursor.getString(cursor.getColumnIndex("_id"));

                intent = new Intent(PlaceViewActivity.this, EventViewActivity.class);
                intent.putExtra("occ_idd", occ_id);
                intent.putExtra("place_idd", place_idd);
                intent.putExtra("event_idd", event_idd);

                startActivity(intent);
            }
        });

   }

    public void fillTitleFields(){  /// ))))))))

        TextView txt_name = (TextView) findViewById(R.id.txt_place_name);
        TextView txt_start_stop = (TextView) findViewById(R.id.txtPlaceStartStopValues);
        try {
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();


            Cursor cursor = db.rawQuery("select * from " +
                    "places p " +
                    " where p.occasion_id="+occ_id+"" +
                    " AND p.place_id="+place_idd+"" +
                    " ORDER by p.start", null);
            if(cursor.moveToFirst()) {
                Calendar start = Calendar.getInstance();
                Calendar stop = Calendar.getInstance();
                start.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndex("start")))*1000);
                stop.setTimeInMillis(Long.parseLong(cursor.getString(cursor.getColumnIndex("end")))*1000);
                Date d_start = start.getTime();
                Date d_stop = stop.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                txt_name.setText(cursor.getString(cursor.getColumnIndex("name")));
                txt_start_stop.setText(sdf.format(d_start)+" - "+sdf.format(d_stop));
            }
            getEvents(occ_id, place_idd);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void getEvents(String occ_id, String place_idd){
        try {
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select e.* from " +
                    "events e," +
                    "places p " +
                    "where p.occasion_id=" + occ_id + "" +
                    " AND p.place_id=" + place_idd + "" +
                    " AND e.occasion_id=p.occasion_id" +
                    " AND e.place_id=p.place_id" +
                    " AND e.start >= p.start" +
                    " AND e.end <= p.end" +
                    " ORDER by e.start", null);


            String[] from = new String[] { "name", "start", "end", "file" };
            int[] to = new int[] { R.id.event_name, R.id.event_start, R.id.event_stop, R.id.event_file };
            CursorViewEvents = new eventViewArrayAdapter(this, cursor, from, to, 0);
            LvEvents.setAdapter(CursorViewEvents);
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
