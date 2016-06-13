package ru.ruorlov.aplikuha;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formUri = "https://collector.tracepot.com/0cd77f9b")
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        super.onCreate();
        ACRA.init(this);
    }
}