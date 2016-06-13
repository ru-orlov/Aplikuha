package ru.ruorlov.aplikuha;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.view.View.OnClickListener;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


//http://aplikuha.ru/#/
public class MainActivity extends AppCompatActivity implements OnClickListener {

    DBHelper dbHelper;
    SQLiteDatabase db;

    SharedPreferences prefs = null;
    private SimpleCursorAdapter mCursorAd;
    private ListView mLv;

    Cursor cursor;



    StringBuffer sb_getData = new StringBuffer();

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://aplikuha.ru");
            //http://lineapp.info
            //mSocket = IO.socket("http://lineapp.info");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ru.ruorlov.aplikuha", MODE_PRIVATE); //записываем отметку о том, что приложение уже установлено

        dbHelper = new DBHelper(this.getBaseContext());
        db = dbHelper.getWritableDatabase();

        mSocket.connect();
        setContentView(R.layout.activity_main);

        Button presentBtn = (Button) findViewById(R.id.btn_present);
        Button pastBtn = (Button) findViewById(R.id.btn_past);
        Button futureBtn = (Button) findViewById(R.id.btn_future);

        presentBtn.setOnClickListener(this);
        pastBtn.setOnClickListener(this);
        futureBtn.setOnClickListener(this);

        mLv = (ListView) findViewById(R.id.lv);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor = ((SimpleCursorAdapter)mLv.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                String occasion_id = cursor.getString(cursor.getColumnIndex("occasion_id"));
                Intent intent = new Intent(MainActivity.this, OccViewActivity.class);
                intent.putExtra("occ_idd", occasion_id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.close();
        mSocket.off();
        dbHelper.db.close();
    }


  /// действия во время первого запуска
    protected void onResume() {
        super.onResume();
        Button presentBtn = (Button) findViewById(R.id.btn_present);
        Button pastBtn = (Button) findViewById(R.id.btn_past);
        Button futureBtn = (Button) findViewById(R.id.btn_future);
        if (prefs.getBoolean("firstrun", true)) {
            // проверка соединения при закпуске.
            // надо сделать сервис, который будет это делать в фоне постоянно
            //System.out.println(isAppRinning());
            firstRunApp();
            getPresentOccasions();
            pastBtn.setEnabled(true);
            presentBtn.setEnabled(false);
            futureBtn.setEnabled(true);
            prefs.edit().putString("stateBtn", "present").commit();
            prefs.edit().putBoolean("firstrun", false).commit();
            // а запущен ли?
            if(!isAppRinning()){
                System.out.println(isAppRinning());
                startService(new Intent(this, MyService.class));
            }
        } else {
            // проверка соединения при закпуске.
            // надо сделать сервис, который будет это делать в фоне постоянно
            //// не первый запуск
            System.out.println("не первый запуск"); // сюда лепить построение списка
           // System.out.println(isAppRinning());
           // myBackThreads();
            getPresentOccasions();
            if (prefs.getString("stateBtn","past").contains("past")){
                pastBtn.setEnabled(false);
                presentBtn.setEnabled(true);
                futureBtn.setEnabled(true);
                getPastOccasions();
            }
            else if (prefs.getString("stateBtn","future").contains("future")) {
                pastBtn.setEnabled(true);
                presentBtn.setEnabled(true);
                futureBtn.setEnabled(false);
                getFutureOccasions();
            }
            else if (prefs.getString("stateBtn","present").contains("present")) {
                pastBtn.setEnabled(true);
                presentBtn.setEnabled(false);
                futureBtn.setEnabled(true);
                getPresentOccasions();
                 }

            if(!isAppRinning()){
            System.out.println(isAppRinning());
                startService(new Intent(this, MyService.class));
            }

        }
    }

    public Boolean isAppRinning(){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(200);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i
                    .next();
            if(runningServiceInfo.service.getClassName().equals("ru.ruorlov.aplikuha.MyService")){
                serviceRunning = true;
            }
        }
        return serviceRunning;
    }


    public void getJsonFromsite() throws URISyntaxException {
        final Long curTime;
        final Socket socket = IO.socket("http://aplikuha.ru");
        //final Socket socket = IO.socket("http://lineapp.info");
        Date now = new Date();
        if (prefs.getBoolean("firstrun", true)) {
            curTime = Long.valueOf(1114475126);
        }
        else {
            curTime = new Long(now.getTime()/1000);
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack ack = new Ack() {
                    @Override
                    public void call(Object... os) {
                        for (Object obj : os) {
                            sb_getData.append(obj);
                        }
                        try {
                            if (sb_getData.length() > 5) {
                                allJsonToDB(sb_getData.toString());
                            }
                        } catch (JSONException | InterruptedException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                        System.out.println(">>>>>> allRecords" + sb_getData.length());
                        sb_getData.setLength(0);
                    }
                };
                Emitter emit = socket.emit("getData", curTime, ack); //1114475126 (NOW   1450787442) 1450877304

                // Receiving an object
                socket.on("refresh", new Emitter.Listener() {
                    @Override
                    public void call(Object... os) {
                        for (Object obj : os) {
                            sb_getData.append(obj);
                        }
                        if (sb_getData.length() > 5) {
                            try {
                                refreshToDB(sb_getData.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("refresh --->"+sb_getData.length());
                        sb_getData.setLength(0);
                    }
                });


            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
    }

    public void refreshToDB(String str) throws JSONException {
        System.out.println(">>>>>>>>>>>>>>>>refresh");

        ContentValues cv = new ContentValues();
        ContentValues cv_occ = new ContentValues();
        ContentValues cv_places = new ContentValues();
        ContentValues cv_events = new ContentValues();
        JSONObject json = new JSONObject(str);
        JSONArray jsonarr = json.getJSONArray("data");

        if ((jsonarr.toString().contains("likes_plus")) || ((jsonarr.toString().contains("likes_minus")))) {

            String r_like_str = null;
            String r_likeType;
            String r_likeIdEvent;
            String r_likeValue;


            if (jsonarr.toString().contains("likes_plus")) {r_like_str = "likes_plus";}
            if (jsonarr.toString().contains("likes_minus")) {r_like_str = "likes_minus";}

            JSONArray jlike  = new JSONArray(json.getString("data"));
            JSONObject lObject = new JSONObject(jlike.getJSONObject(0).toString());
            if (lObject.getString("type").contains("events"))   {r_likeType = "events";}
            if (lObject.getString("type").contains("places"))   {r_likeType = "places";}
            if (lObject.getString("type").contains("occasions")){r_likeType = "occasions";}
                 r_likeIdEvent = jlike.getJSONObject(0).getString("id"); // event_id 385
                 r_likeValue = lObject.getJSONObject("data").getString(r_like_str).toString();  //2

            //        statmt.execute("UPDATE "+likeType+" SET "+like_str+"="+like_str+"+"+likeValue+" "
            //                      + "WHERE "
            //                      + "event_id = "+likeIdEvent+"");

            //          System.out.println(likeType+ " "+ likeIdEvent + " " + likeValue);

        }
        else {
            if (jsonarr.getJSONObject(0).toString().contains("\"type\":\"comments\"")) {

                String r_commTypeEvent;
                String r_commIdEvent;
                String r_commId;

                JSONArray jcomment = new JSONArray(json.getString("data"));
                JSONObject cObject = new JSONObject(jsonarr.getJSONObject(1).toString());
                if (cObject.getString("type").contains("events")) {
                    r_commTypeEvent = "1";
                }
                if (cObject.getString("type").contains("places")) {
                    r_commTypeEvent = "2";
                }
                if (cObject.getString("type").contains("occasions")) {
                    r_commTypeEvent = "3";
                }
                r_commIdEvent = jcomment.getJSONObject(0).getString("id");
                r_commId = cObject.getJSONObject("data").getString("comments").toString();

  /*         statmt.execute("INSERT INTO comments ("
                         + "'event_type',"
                         + "'event_id', "
                         + "'comment_id') "
                         + "VALUES ("
                         + ""+commTypeEvent+","
                         + ""+commIdEvent+","
                         + "'"+commId+"'); ");   */
            } else {

                JSONObject dataObj = new JSONObject(jsonarr.getJSONObject(0).getString("data"));
                String objType = jsonarr.getJSONObject(0).getString("type").toString();   //tableName
                if (objType.equals("occasions")) {

                    String r_occ_name;
                    String r_occ_start;
                    String r_occ_file; //image
                    String r_occ_description;
                    String r_occ_duration_e;
                    
                    JSONObject dataObj_second = new JSONObject(jsonarr.getJSONObject(1).getString("data"));
                    r_occ_file = dataObj.getString("image");
                    r_occ_name = dataObj.getString("name");
                    r_occ_start = dataObj.getString("start");
                    r_occ_description = dataObj.getString("description");
                    r_occ_duration_e = dataObj.getString("duration_e");
                    cv.put("file", r_occ_file);
                    cv.put("name", r_occ_name);
                    cv.put("start", r_occ_start);
                    cv.put("description", r_occ_description);
                    cv.put("duration_e", r_occ_duration_e);
                    dbHelper.db.update("occasions", cv, "occasion_id=" + dataObj.getString("id").toString(), null);
                    cv.clear();

                    //////////////
                    if (dataObj.getString("params").toString().contains("{")) {
                        dbHelper.db.delete("params", "param_id=" + dataObj.getString("id").toString() + " AND param_type = 1", null);

                        JSONObject params_occ = dataObj.getJSONObject("params");
                        Iterator<?> occ_params_keys = params_occ.keys();
                        while (occ_params_keys.hasNext()) {
                            String occ_param_key = (String) occ_params_keys.next();
                            cv_occ.put("param_type", 1);
                            cv_occ.put("param_id", dataObj.getString("id"));
                            cv_occ.put("param_name", occ_param_key);
                            cv_occ.put("param_value", params_occ.getString(occ_param_key));
                            dbHelper.db.insert("params", null, cv_occ);
                            cv_occ.clear();
                        }
                    }
                }
                // places table
                if (objType.equals("places")) {
                    String r_pl_occasion_id;
                    String r_pl_file;
                    String r_pl_name;
                    String r_pl_start;
                    String r_pl_description;
                    
                    r_pl_occasion_id = dataObj.getString("occasion_id");
                    r_pl_file = dataObj.getString("image");
                    r_pl_name = dataObj.getString("name");
                    r_pl_start = dataObj.getString("start");
                    r_pl_description = dataObj.getString("description");
                    cv_places.put("occasion_id", r_pl_occasion_id);
                    cv_places.put("file", r_pl_file);
                    cv_places.put("name", r_pl_name);
                    cv_places.put("start", r_pl_start);
                    cv_places.put("description", r_pl_description);
                    dbHelper.db.update("places", cv_places, "_id=" + dataObj.getString("id").toString(), null);
                    cv_places.clear();
                }

                // events table
                if (objType.equals("events")) {
                    String r_ev_name;
                    String r_ev_start;
                    String r_ev_end;
                    String r_ev_description;
                    String r_ev_duration;
                    String r_ev_position;

                    ////// refresh events
                    r_ev_duration = dataObj.getString("duration");
                    r_ev_name = dataObj.getString("name");
                    r_ev_start = dataObj.getString("start");
                    r_ev_description = dataObj.getString("description");
                    r_ev_end = dataObj.getString("end");
                    r_ev_position = dataObj.getString("position");
                    cv_events.put("duration", r_ev_duration);
                    cv_events.put("name", r_ev_name);
                    cv_events.put("start", r_ev_start);
                    cv_events.put("description", r_ev_description);
                    cv_events.put("end", r_ev_end);
                    cv_events.put("position", r_ev_position);
                    dbHelper.db.update("events", cv_events, "event_id=" + dataObj.getString("id").toString(), null);
                    cv_events.clear();

                    //////////////
                    if (dataObj.getString("params").toString().contains("{")) {
                        dbHelper.db.delete("params", "param_id=" + dataObj.getString("id").toString() + " AND param_type = 2", null);

                        JSONObject params_events = dataObj.getJSONObject("params");
                        Iterator<?> events_params_keys = params_events.keys();
                        while (events_params_keys.hasNext()) {
                            String event_param_key = (String) events_params_keys.next();
                            cv_events.put("param_type", 2);
                            cv_events.put("param_id", dataObj.getString("id"));
                            cv_events.put("param_name", event_param_key);
                            cv_events.put("param_value", params_events.getString(event_param_key));
                            dbHelper.db.insert("params", null, cv_events);
                            cv_events.clear();
                        }
                    }
                }
            }
        }
        dbHelper.db.close();
    }
    public void firstRunApp(){
        System.out.println("первый запуск");

        try {
            prefs.edit().putBoolean("firstrun", true).commit();
            getJsonFromsite();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public void alertWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.//setTitle("заголовок окна")
                setMessage("Проверьте интернет соединение или попробуйте позже.")
                // .setIcon()
                .setCancelable(false)
                .setNegativeButton("Ok...(((",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void getPresentOccasions(){
            cursor = dbHelper.selectFromDB("SELECT * FROM " +
                                           "occasions o " +
                                           "WHERE strftime('%s','now') BETWEEN o.start AND o.end " +
                                           "ORDER by o.start");
            String[] from = new String[] { "occasion_id", "name", "start", "end", "file" };
            int[] to = new int[] { R.id.occId, R.id.occName, R.id.occStart, R.id.occEnd, R.id.occFile };

            mCursorAd = new occViewArrayAdapterAll(this, cursor, from, to, 0);
            mLv.setAdapter(mCursorAd);
    }

    public void getPastOccasions(){
            cursor = dbHelper.selectFromDB("SELECT * FROM " +
                        "occasions o " +
                        "WHERE o.end <= strftime('%s','now') " +
                        "ORDER by o.start");
            String[] from = new String[] { "occasion_id", "name", "start", "end", "file" };
            int[] to = new int[] { R.id.occId, R.id.occName, R.id.occStart, R.id.occEnd, R.id.occFile };

            mCursorAd = new occViewArrayAdapterAll(this, cursor, from, to, 0);
            mLv.setAdapter(mCursorAd);
    }

    public void getFutureOccasions(){
            cursor = dbHelper.selectFromDB("SELECT * FROM " +
                    "occasions o " +
                    "WHERE o.start >= strftime('%s','now') " +
                    "ORDER by o.start");

            String[] from = new String[] { "occasion_id", "name", "start", "end", "file" };
            int[] to = new int[] { R.id.occId, R.id.occName, R.id.occStart, R.id.occEnd, R.id.occFile };

            mCursorAd = new occViewArrayAdapterAll(this, cursor, from, to, 0);
            mLv.setAdapter(mCursorAd);
    }

    @Override
    public void onClick(View v) {
        Button presentBtn = (Button) findViewById(R.id.btn_present);
        Button pastBtn = (Button) findViewById(R.id.btn_past);
        Button futureBtn = (Button) findViewById(R.id.btn_future);


        switch (v.getId()) {
            case R.id.btn_past:
                pastBtn.setEnabled(false);
                presentBtn.setEnabled(true);
                futureBtn.setEnabled(true);
                prefs.edit().putString("stateBtn", "past").commit();
                System.out.println(prefs.getString("stateBtn","past"));
                getPastOccasions();
            break;
            case R.id.btn_present:
                pastBtn.setEnabled(true);
                presentBtn.setEnabled(false);
                futureBtn.setEnabled(true);
                prefs.edit().putString("stateBtn", "present").commit();
                System.out.println(prefs.getString("stateBtn", "present"));
                getPresentOccasions();
            break;
            case R.id.btn_future:
                pastBtn.setEnabled(true);
                presentBtn.setEnabled(true);
                futureBtn.setEnabled(false);
                prefs.edit().putString("stateBtn", "future").commit();
                System.out.println(prefs.getString("stateBtn", "future"));
                getFutureOccasions();
            break;
        }
    }




    public void allJsonToDB(String body) throws JSONException, URISyntaxException, InterruptedException {
        System.out.println("start...");
        JSONObject jObject = new JSONObject(body);
        JSONObject data = jObject.getJSONObject("data");
        ContentValues cv = new ContentValues();
        ContentValues cv_refresh = new ContentValues();
        ContentValues cv_events = new ContentValues();
        ContentValues cv_ocasions = new ContentValues();
        ////////////////////  places
        String pl_occasion_id;
        String pl_place_id;
        String pl_name;
        String pl_start;
        String pl_end;
        String pl_description;
        String pl_file;
        
        JSONObject places = data.getJSONObject("places");
        Iterator<?> places_keys = places.keys();
        dbHelper.db.beginTransaction();
        while (places_keys.hasNext()) {
            String pl_key = (String) places_keys.next();
            if (places.get(pl_key) instanceof JSONObject) {
                if (places.get(pl_key).toString().contains("id")) {
                    JSONObject jplaces = new JSONObject(places.get(pl_key).toString());
                    if (places.get(pl_key).toString().contains("occasion_id")){pl_occasion_id = jplaces.getString("occasion_id");} else {pl_occasion_id = "";}
                    if (places.get(pl_key).toString().contains("id")){pl_place_id = jplaces.getString("id");} else {pl_place_id = "";}
                    if (places.get(pl_key).toString().contains("name")){pl_name = jplaces.getString("name");} else {pl_name = "";}
                    if (places.get(pl_key).toString().contains("start")){pl_start = jplaces.getString("start");} else {pl_start = "";}
                    if (places.get(pl_key).toString().contains("end")){pl_end = jplaces.getString("end");} else {pl_end = "";}
                    if (places.get(pl_key).toString().contains("description")){pl_description = jplaces.getString("description");} else {pl_description = "";}
                    if (places.get(pl_key).toString().contains("image")){pl_file = jplaces.getString("image");} else {pl_file = "";}
                    cv.put("occasion_id", pl_occasion_id);
                    cv.put("place_id", pl_place_id);
                    cv.put("name", pl_name);
                    cv.put("start", pl_start);
                    cv.put("end", pl_end);
                    cv.put("description", pl_description);
                    cv.put("file", pl_file);
                    JSONArray place_events = jplaces.getJSONArray("events");
                    for (int i = 0; i < place_events.length(); i++) {
                        cv_events.put("place_id", pl_place_id);
                        cv_events.put("event_id", String.valueOf(place_events.get(i)));
                        int events_rowID = dbHelper.db.update("place_events", cv_events, "place_id = " + pl_place_id ,null);
                        if (events_rowID<1) {dbHelper.db.insert("place_events", null, cv_events);}

                        cv_events.clear();
                    }
                    int place_rowID = dbHelper.db.update("places", cv, "place_id = " + jplaces.getString("id") ,null);
                    if (place_rowID<1) {dbHelper.db.insert("places", null, cv);}
                    cv.clear();
                }
            }
        }
        System.out.println("places...");

        ////////////////////  events
        String ev_occasion_id;
        String ev_place_id;
        String ev_name;
        String ev_comments;
        String ev_start;
        String ev_end;
        String ev_likes_minus;
        String ev_likes_plus;
        String ev_description;
        String ev_duration;
        String ev_file;
        String ev_position;
        String ev_paused;

        JSONObject events = data.getJSONObject("events");
        Iterator<?> events_keys = events.keys();
        while (events_keys.hasNext()) {
            String e_key = (String) events_keys.next();
            if (events.get(e_key) instanceof JSONObject) {
                if (events.get(e_key).toString().contains("id")) {
                    JSONObject jevets = new JSONObject(events.get(e_key).toString());
                    if (events.get(e_key).toString().contains("occasion_id")){ev_occasion_id = jevets.getString("occasion_id");} else {ev_occasion_id = "";}
                    if (events.get(e_key).toString().contains("place_id")){ev_place_id = jevets.getString("place_id");} else {ev_place_id = "";}
                    if (events.get(e_key).toString().contains("name")){ev_name = jevets.getString("name");} else {ev_name = "";}
                    if (events.get(e_key).toString().contains("comments")){ev_comments = jevets.getString("comments");} else {ev_comments = "";}
                    if (events.get(e_key).toString().contains("start")){ev_start = jevets.getString("start");} else {ev_start = "";}
                    if (events.get(e_key).toString().contains("end")){ev_end = jevets.getString("end");} else {ev_end = "";}
                    if (events.get(e_key).toString().contains("likes_minus")){ev_likes_minus = jevets.getString("likes_minus");} else {ev_likes_minus = "0";}
                    if (events.get(e_key).toString().contains("likes_plus")){ev_likes_plus = jevets.getString("likes_plus");} else {ev_likes_plus = "0";}
                    if (events.get(e_key).toString().contains("description")){ev_description = jevets.getString("description");} else {ev_description = "";}
                    if (events.get(e_key).toString().contains("duration")){ev_duration = jevets.getString("duration");} else {ev_duration = "";}
                    if (events.get(e_key).toString().contains("image")){ev_file = jevets.getString("image");} else {ev_file = "";}
                    if (events.get(e_key).toString().contains("position")){ev_position = jevets.getString("position");} else {ev_position = "";}
                    if (events.get(e_key).toString().contains("paused")){ev_paused = jevets.getString("paused");} else {ev_paused = "";}
                    cv.put("event_id", jevets.getString("id"));
                    cv.put("occasion_id", ev_occasion_id);
                    cv.put("place_id", ev_place_id);
                    cv.put("name", ev_name);
                    cv.put("comments", ev_comments);
                    cv.put("start", ev_start);
                    cv.put("likes_minus", ev_likes_minus);
                    cv.put("likes_plus", ev_likes_plus);
                    cv.put("end", ev_end);
                    cv.put("description", ev_description);
                    cv.put("duration", ev_duration);
                    cv.put("file", ev_file);
                    cv.put("position", ev_position);
                    cv.put("paused", ev_paused);

                    int event_rowID = dbHelper.db.update("events", cv, "event_id = " + jevets.getString("id") ,null);
                    if (event_rowID<1) {dbHelper.db.insert("events", null, cv);}

                    //  System.out.println("events inserted, ID = " + rowID);
                    cv.clear();
                }
            }
        }
        System.out.println("events...");
        /////////////////////  occasions
        String occ_t1;
        String occ_t2;
        String occ_t3;
        String occ_status;
        String occ_id;
        String occ_name;
        String occ_start;
        String occ_end;
        String occ_paused;
        String occ_file;
        String occ_description;
        String occ_duration_e;
        JSONObject occasions = data.getJSONObject("occasions");
        Iterator<?> occ_keys = occasions.keys();
        while (occ_keys.hasNext()) {
            String occ_key = (String) occ_keys.next();
            if (occasions.get(occ_key) instanceof JSONObject) {
                if (occasions.get(occ_key).toString().contains("id")) {
                    JSONObject jOccasions = new JSONObject(occasions.get(occ_key).toString());
                    if (occasions.get(occ_key).toString().contains("t1")){occ_t1 = jOccasions.getString("t1");} else {occ_t1 = "";}
                    if (occasions.get(occ_key).toString().contains("t2")){occ_t2 = jOccasions.getString("t2");} else {occ_t2 = "";}
                    if (occasions.get(occ_key).toString().contains("t3")){occ_t3 = jOccasions.getString("t3");} else {occ_t3 = "";}
                    if (occasions.get(occ_key).toString().contains("id")){occ_id = jOccasions.getString("id");} else {occ_id = "";}
                    if (occasions.get(occ_key).toString().contains("status")){occ_status = jOccasions.getString("status");} else {occ_status = "";}
                    if (occasions.get(occ_key).toString().contains("name")){occ_name = jOccasions.getString("name");} else {occ_name = "";}
                    if (occasions.get(occ_key).toString().contains("start")){occ_start = jOccasions.getString("start");} else {occ_start = "";}
                    if (occasions.get(occ_key).toString().contains("end")){occ_end = jOccasions.getString("end");} else {occ_end = occ_start;}
                    if (occasions.get(occ_key).toString().contains("paused")) {occ_paused = jOccasions.getString("paused");} else {occ_paused = "";}
                    if (occasions.get(occ_key).toString().contains("description")){occ_description = jOccasions.getString("description");} else {occ_description = "";}
                    if (occasions.get(occ_key).toString().contains("duration_e")){occ_duration_e = jOccasions.getString("duration_e");} else {occ_duration_e = "";}
                    if (occasions.get(occ_key).toString().contains("image")){occ_file = jOccasions.getString("image");} else {occ_file = "";}
                    cv.put("t1", occ_t1);
                    cv.put("t2", occ_t2);
                    cv.put("t3", occ_t3);
                    cv.put("occasion_id", occ_id);
                    cv.put("status", occ_status);
                    cv.put("name", occ_name);
                    cv.put("start", occ_start);
                    cv.put("end", occ_end);
                    cv.put("paused", occ_paused);
                    cv.put("description", occ_description);
                    cv.put("duration_e", occ_duration_e);
                    cv.put("file", occ_file);
                    if (occasions.get(occ_key).toString().contains("places")) {
                        JSONArray place_occasions = jOccasions.getJSONArray("places");
                        for (int i = 0; i < place_occasions.length(); i++) {
                            cv_ocasions.put("occasion_id", jOccasions.getString("id"));
                            cv_ocasions.put("place_id", String.valueOf(place_occasions.get(i)));
                            dbHelper.db.insert("occasion_places", null, cv_ocasions);
                            cv_ocasions.clear();
                        }
                    }

                    if (jOccasions.getString("params").toString().contains("{"))    {
                        JSONObject params_occ = jOccasions.getJSONObject("params");
                        Iterator<?> occ_params_keys = params_occ.keys();
                        while (occ_params_keys.hasNext()) {
                            String occ_param_key = (String) occ_params_keys.next();
                            cv_refresh.put("param_type", 1);
                            cv_refresh.put("param_id", jOccasions.getString("id"));
                            cv_refresh.put("param_name", occ_param_key);
                            cv_refresh.put("param_value", params_occ.getString(occ_param_key));

                            int param_rowID = dbHelper.db.update("params", cv_refresh, "param_id = " + jOccasions.getString("id") ,null);
                            if (param_rowID<1) {dbHelper.db.insert("params", null, cv_refresh);}
                            cv_refresh.clear();
                        }
                    }



                    int occ_rowID = dbHelper.db.update("occasions", cv, "occasion_id=" + jOccasions.getString("id").toString(), null);
                    if (occ_rowID<1) {dbHelper.db.insert("occasions", null, cv);}
                    cv.clear();
                }
            }
        }

        dbHelper.db.setTransactionSuccessful();
        dbHelper.db.endTransaction();
        System.out.println("occasions and done...");
        dbHelper.db.close();

        //myBackThreads();
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
