package ru.ruorlov.aplikuha;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by User on 19.12.2015.
 */
public class EventViewActivity extends AppCompatActivity {
    DBHelper dbHelper;
    private SimpleCursorAdapter mCursorViewEvents;

    private ListView LvEventComments;
    private TextView txt_event_duration;
  //  private EditText mCommMessage;

    public String cnt_comments;

    StringBuffer sb_commentData = new StringBuffer();

    Intent intent;
    String event_idd;
    String comm_event_id;
    private Socket mSocket;

    Handler h;

    Button sendComment;
    Button likePlus;
    Button likeMinus;
    TextView commentText;

    StringBuffer sb_getData = new StringBuffer();


    {
        try {
            mSocket = IO.socket("http://aplikuha.ru");
            //mSocket = IO.socket("http://lineapp.info");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);
        dbHelper = new DBHelper(this.getBaseContext());
        intent = getIntent();
        event_idd = intent.getStringExtra("event_idd");
        LvEventComments = (ListView) findViewById(R.id.lv_elent_comments_view);
        txt_event_duration = (TextView) findViewById(R.id.txtEventDurationValues);
        mSocket.connect();

        sendComment = (Button) findViewById(R.id.comm_btn_send);

        commentText = (TextView) findViewById(R.id.editText);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                updateUI();
           }
        };

        View.OnClickListener listemSendComment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendComment("add_comment", comm_event_id);
                } catch (URISyntaxException | JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        sendComment.setOnClickListener(listemSendComment);

    /*    likeMinus = (Button) findViewById(R.id.ev_like_minus);
        View.OnClickListener listenLikeMinus = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    likeEvent(comm_event_id, "likes_minus");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        };
        likeMinus.setOnClickListener(listenLikeMinus);

        likePlus = (Button) findViewById(R.id.ev_like_plus);
        View.OnClickListener listenLikePlus = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    likeEvent(comm_event_id, "likes_plus");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        };
        likePlus.setOnClickListener(listenLikePlus);

    */
        fillTitleFields();

        try {
            getJsoncomments(comm_event_id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.close();
        mSocket.off();
        dbHelper.db.close();
    }

    public void updateUI() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
       // fillTitleFields();
        fillListComments();
    }

    public void fillTitleFields(){
        Cursor cursorEventFill;
        TextView txt_name = (TextView) findViewById(R.id.txt_event_name);
        TextView txt_start_stop = (TextView) findViewById(R.id.txtEventStartStopValues);

        ImageView imgEventPlus = (ImageView) findViewById(R.id.ev_like_plus);
        ImageView imgEventMinus = (ImageView) findViewById(R.id.ev_like_minus);
        TextView txt_valueplus = (TextView) findViewById(R.id.valueLikePlus);
        TextView txt_valueminus = (TextView) findViewById(R.id.valueLikeMinus);
        String str_hours;


            cursorEventFill = dbHelper.selectFromDB("SELECT * FROM " +
                    "events e " +
                    "WHERE e._id="+event_idd);

            if(cursorEventFill.moveToFirst()) {
                Calendar start = Calendar.getInstance();
                Calendar stop = Calendar.getInstance();
                Calendar dur = Calendar.getInstance();
                start.setTimeInMillis(Long.parseLong(cursorEventFill.getString(cursorEventFill.getColumnIndex("start")))*1000);
                stop.setTimeInMillis(Long.parseLong(cursorEventFill.getString(cursorEventFill.getColumnIndex("end")))*1000);
                dur.setTimeInMillis(Long.parseLong(cursorEventFill.getString(cursorEventFill.getColumnIndex("duration")))*1000);
                Date d_start = start.getTime();
                Date d_stop = stop.getTime();
                Date d_dur = dur.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                SimpleDateFormat sdf_end = new SimpleDateFormat("HH:mm");
                txt_name.setText(cursorEventFill.getString(cursorEventFill.getColumnIndex("name")));

                comm_event_id = cursorEventFill.getString(cursorEventFill.getColumnIndex("event_id"));//event_id

                txt_start_stop.setText(sdf.format(d_start) + " - " + sdf_end.format(d_stop));
                imgEventPlus.setImageResource(R.drawable.like_plus);
                imgEventMinus.setImageResource(R.drawable.like_minus);
                txt_valueplus.setText(cursorEventFill.getString(cursorEventFill.getColumnIndex("likes_plus")));
                txt_valueminus.setText(cursorEventFill.getString(cursorEventFill.getColumnIndex("likes_minus")));

                cnt_comments = cursorEventFill.getString(cursorEventFill.getColumnIndex("comments"));

                int hours = dur.get(Calendar.HOUR);
             //   if (hours == 0) {str_hours = "";} else {str_hours = dur.get(Calendar.HOUR)+"ч.";}

               // txt_event_duration.setText(str_hours + " " + dur.get(Calendar.MINUTE) + ":" + dur.get(Calendar.SECOND));
            }

    }

    public void fillListComments() {
        Cursor cursorEventCommentsFill;
        cursorEventCommentsFill = dbHelper.selectFromDB("SELECT * FROM " +
                                                        "event_comment c " +
                                                        "WHERE c.event_id="+comm_event_id+" ORDER BY c.entered DESC");
        String[] from = new String[] { "comment_id", "author_name", "is_admin", "message", "entered", "filename" };
        int[] to = new int[] { R.id.comment_id, R.id.comment_author_name, R.id.comment_is_admin , R.id.comment_message, R.id.comment_entered, R.id.comment_file };

        mCursorViewEvents = new commentsViewArrayAdapter(this, cursorEventCommentsFill, from, to, 0);
        LvEventComments.setAdapter(mCursorViewEvents);

    }


    public void getJsoncomments(final String comm_event_id) throws URISyntaxException {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack ack = new Ack() {
                    @Override
                    public void call(Object... os) {
                        for (Object obj : os) {
                            sb_commentData.append(obj);
                        }
                        if (sb_commentData.length() > 5) {
                            try {
                                allCommToDB(sb_commentData.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        sb_commentData.setLength(0);
                    }
                };

                final JSONObject comment_prop = new JSONObject();
                try {
                    comment_prop.put("id", comm_event_id);
                    comment_prop.put("start", 0);
                    comment_prop.put("page", 1);
                    comment_prop.put("showAll", true);
                    comment_prop.put("ck", 10);
                } catch (JSONException ex) {

                }
                System.out.println(comm_event_id);
                Emitter emit = mSocket.emit("get_comments", comment_prop, ack);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });


    }

    private void sendComment(final String event, final String id) throws URISyntaxException, JSONException {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {


            @Override
            public void call(Object... args) {
                Ack ack = new Ack() {
                    @Override
                    public void call(Object... os) {

                    }
                };

                if (event.contains("add_comment")) {
                    System.out.println("зашли..");

                    final JSONObject comment_prop = new JSONObject();
                    try {
                        comment_prop.put("id", id);
                        comment_prop.put("text", commentText.getText().toString());
                        comment_prop.put("file", "");
                    } catch (JSONException ex) {
                        System.out.println("");
                    }
                    Emitter emit = mSocket.emit("add_comment", comment_prop, ack);
                }




            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                commentText.setText("");
            }

         });
       // commentText.setText("");
    }

    public void likeEvent(final String comm_event_id, final String like_str) throws URISyntaxException {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack ack = new Ack() {
                    @Override
                    public void call(Object... os) {
                        for (Object obj : os) {
                            sb_commentData.append(obj);
                        }
                        updateUI();
                    }
                };

                final JSONObject comment_prop = new JSONObject();
                try {
                    comment_prop.put("id", comm_event_id);
                    comment_prop.put("like", like_str);
                } catch (JSONException ex) {

                }
                System.out.println(comm_event_id);
                Emitter emit = mSocket.emit("like_event", comment_prop, ack);
                //remove_like_event
                //likes_plus' : 'likes_minus
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }
        });
    }

    public void allCommToDB(String comm_str) throws JSONException {
        System.out.println(comm_str);

        JSONObject jComment = new JSONObject(comm_str);
        JSONArray commentArr = jComment.getJSONArray("comments");
        ContentValues cv_comments = new ContentValues();
        String comm_id;
        String comm_event_id = null;
        String comm_author_name;
        String comm_author_social;
        String comm_author;
        String comm_entered;
        String comm_message;
        String comm_is_admin;
        String comm_filename;
        String comm_extension;
        String comm_dot;
        for (int i = 0; i < commentArr.length(); i++) {
            comm_id = commentArr.getJSONObject(i).getString("_id");
            comm_event_id = commentArr.getJSONObject(i).getString("eventId");
            if (commentArr.getJSONObject(i).toString().contains("authorName")){
                comm_author_name = commentArr.getJSONObject(i).getString("authorName");} else {comm_author_name = "";}
            if (commentArr.getJSONObject(i).toString().contains("authorSocial")){
                comm_author_social = commentArr.getJSONObject(i).getString("authorSocial");} else {comm_author_social = "";}
            if (commentArr.getJSONObject(i).toString().contains("author")){
                comm_author = commentArr.getJSONObject(i).getString("author");} else {comm_author = "";}
            if (commentArr.getJSONObject(i).toString().contains("entered")){
                comm_entered = commentArr.getJSONObject(i).getString("entered");} else {comm_entered = "";}
            if (commentArr.getJSONObject(i).toString().contains("message")){
                comm_message = commentArr.getJSONObject(i).getString("message");} else {comm_message = "";}
            if (commentArr.getJSONObject(i).toString().contains("isAdmin")){
                comm_is_admin = commentArr.getJSONObject(i).getString("isAdmin");} else {comm_is_admin = "";}
            if (commentArr.getJSONObject(i).toString().contains("filename")){
                comm_filename = commentArr.getJSONObject(i).getString("filename");
                comm_dot = ".";} else {comm_filename = ""; comm_dot = "";}
            if (commentArr.getJSONObject(i).toString().contains("extension")){
                comm_extension = commentArr.getJSONObject(i).getString("extension");} else {comm_extension = "";}

            cv_comments.put("comment_id", comm_id);
            cv_comments.put("entered", comm_entered);
            cv_comments.put("event_id", comm_event_id);
            cv_comments.put("message", comm_message);
            cv_comments.put("is_admin", comm_is_admin);
            cv_comments.put("filename", comm_filename+comm_dot+comm_extension);
            cv_comments.put("author_name", comm_author_name);
            cv_comments.put("author", comm_author);
            cv_comments.put("author_social", comm_author_social);
            int comment_rowID = dbHelper.db.update("event_comment", cv_comments, "comment_id ='"+comm_id+"'",null);
            if (comment_rowID<1) {dbHelper.db.insert("event_comment", null, cv_comments);}

        }
        cv_comments.clear();

        h.sendEmptyMessage(0);
    }
}
