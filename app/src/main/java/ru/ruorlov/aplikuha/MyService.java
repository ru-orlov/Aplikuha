package ru.ruorlov.aplikuha;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    SharedPreferences prefs = null;
    MainActivity mActivity = new MainActivity();
    public MyService() {
    }

    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        try {
            someTask();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }

    public IBinder onBind(Intent intent) {
        System.out.println("onBind");
        return null;
    }

    void someTask() throws URISyntaxException, InterruptedException {
        /*
                while (true) {
                    try {
                        System.out.println(">>>>>>>>>>>>service has start...");
                        TimeUnit.SECONDS.sleep(5);
                        mActivity.getJsonFromsite();
                    } catch (InterruptedException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
        */
            }
}
