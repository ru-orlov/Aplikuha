package ru.ruorlov.aplikuha;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


//http://aplikuha.ru/#/occasions/view/#occ_id
public class OccViewActivity extends AppCompatActivity {

    DBHelper dbHelper;
    private SimpleCursorAdapter CursorViewPlaces;
    private ListView Lvplaces;
    Intent intent;
    String occasion_id;
    String place_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.occasion_view);
        Lvplaces = (ListView) findViewById(R.id.lv_occ_view);
        fillTitleFields();

        Lvplaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((occViewArrayAdapter) Lvplaces.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                occasion_id = cursor.getString(cursor.getColumnIndex("occasion_id"));
                place_id = cursor.getString(cursor.getColumnIndex("place_id"));
                intent = new Intent(OccViewActivity.this, PlaceViewActivity.class);
                intent.putExtra("occ_idd", occasion_id);
                intent.putExtra("place_idd", place_id);
                startActivity(intent);
            }
        });
   }

    public void fillTitleFields(){  /// ))))))))

        TextView txt_name = (TextView) findViewById(R.id.txt_event_name);
        TextView txt_start_stop = (TextView) findViewById(R.id.txtStartStopValues);
        try {
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Intent intent = getIntent();
            String occ_id = intent.getStringExtra("occ_idd");

            Cursor cursor = db.query("occasions", null, "occasion_id=" + occ_id, null, null, null, null);
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
            getPlaces(occ_id);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void getPlaces(String occ_id){
        try {
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query("places", null, "occasion_id=" + occ_id, null, null, null, null);

            String[] from = new String[] { "name", "start", "end", "file" };

            int[] to = new int[] { R.id.occName, R.id.occStart, R.id.occEnd, R.id.occFile };
            CursorViewPlaces = new occViewArrayAdapter(this, cursor, from, to, 0);
            Lvplaces.setAdapter(CursorViewPlaces);
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(), "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

    }

}

